package chatbot
import org.jsoup._
import collection.JavaConverters._
import sttp.client4.quick._
import sttp.model.StatusCode
//import java.io.File

import Database.createDbSession
import dataclass.DbConfig
import dataclass.EmbeddingData
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import skunk._
import skunk.implicits._
import skunk.codec.all._

import cats.effect.{IO, IOApp, Resource, ExitCode}
import cats.implicits._
import io.circe.parser._


object WebScrape extends IOApp {
  // HuggingFace models for tokenization
  private val embeddingModel = "sentence-transformers/all-MiniLM-L6-v2"
  private val embeddingUrl = s"https://api-inference.huggingface.co/pipeline/feature-extraction/$embeddingModel"

  // Sites to scrape
  val excludedContents = List("FAQs", "Related Content")
  val websites : List[String] = List(
    "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/earned-income-relief"
  )


  def scrapeWebsite(site: String): IO[String] = {

      val doc = Jsoup.connect(site)
        .timeout(10000)
        .userAgent("Mozilla/5.0")
        .get()

//      val file = new File("src/main/scala/chatbot/IRAS.html")
//      val doc = Jsoup.parse(file)

      val resultsBuffer = doc.select("section").asScala.flatMap { section =>
        val h2 = section.select("h2")
        if (h2.attr("id").startsWith("title") &&
          !excludedContents.exists(exclude => h2.text().contains(exclude)))
          Some(section.text())
        else None
      }

      val results = resultsBuffer.mkString("\n")
      if (results.isEmpty) {
        IO.raiseError(new Exception(s"Content is empty"))
      } else {
        IO.pure(results)
      }
    }

  def getEmbeddings(text: String): IO[Array[Double]] = {
    for {
      token <- IO(sys.env.getOrElse("HF_TOKEN", ""))
        .ensure(new Exception("HF_TOKEN environment variable not set"))(_.nonEmpty)

      response <- IO(quickRequest
        .post(uri"$embeddingUrl")
        .auth.bearer(token)
        .body(text)
        .send())

      _ <- if (response.code == StatusCode.Ok) IO.unit
      else IO.raiseError(new Exception(s"API request failed with status: ${response.statusText}"))

      embedding <- IO.fromEither(
        decode[Array[Double]](response.body)
          .left.map(err => new Exception(s"Failed to parse response: $err"))
      )
    } yield embedding
  }


  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val session = createDbSession(config)

    val codec: Codec[EmbeddingData] =
      (varchar ~ Array[Double]).imap {
        case (content, embeddings) => EmbeddingData(content, embeddings)
      }(embeddingData => (embeddingData.content, embeddingData.embeddings))

    val insertEmbedding: Command[EmbeddingData] =
      sql"""
           INSERT INTO data
           VALUES ($codec)
         """.command

    websites.traverse { site =>
      for {
        contents <- scrapeWebsite(site)
        embeddings <- getEmbeddings(contents)
//        _ <- IO(println(s"Embeddings = ${embeddings.mkString("Array(", ", ", ")")}"))
        _ <- session.use { s =>
          s.prepare(insertEmbedding).use { cmd =>
            cmd.execute(EmbeddingData(contents, embeddings))
          }
        }
      } yield ()
      }.as(ExitCode.Success)
    }

}
