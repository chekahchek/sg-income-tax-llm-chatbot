package chatbot

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.Update0
import org.jsoup._
import collection.JavaConverters._
import sttp.client4.quick._
import sttp.model.StatusCode
import Database.createDbSession
import dataclass.DbConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import cats.effect.{IO, IOApp, ExitCode}
import cats.implicits._
import io.circe.parser._
//import scala.annotation.unused
//import java.io.File


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


  def insertTextAndEmbeddings(id: String, embedding: Array[Double]): Update0 = {
    sql"""
         INSERT INTO tax (id, embedding)
         VALUES ($id, $embedding)
       """.update
  }


  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor = createDbSession(config)


    websites.traverse { site =>
      for {
        contents <- scrapeWebsite(site)
        embedding <- getEmbeddings(contents)
//        embedding = Array(0.1, 0.2, 0.3)
        _ <- transactor.use {xa =>
        insertTextAndEmbeddings(contents, embedding).run.transact(xa)
      }
      } yield ()
      }.as(ExitCode.Success)
    }

}