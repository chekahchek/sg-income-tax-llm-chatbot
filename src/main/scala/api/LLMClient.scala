package api


import sttp.client4.quick._
import sttp.model.StatusCode
import cats.effect.IO
import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser._


object LLMClient {

  case class EmbeddingData(embedding: Array[Double])
  case class EmbeddingResponse(data: List[EmbeddingData])

  // HuggingFace models for tokenization
  private val embeddingModel = "text-embedding-3-small"
  private val embeddingUrl = "https://api.openai.com/v1/embeddings"


  def getEmbeddings(text: String): IO[Array[Double]] = {
    val strippedText = text.replace("\n", "")
    val jsonPayload = s"""{"input": "$strippedText", "model": "$embeddingModel"}"""
    for {
      token <- IO(sys.env.getOrElse("API_KEY", ""))
        .ensure(new Exception("API_KEY environment variable not set"))(_.nonEmpty)

      response <- IO(quickRequest
        .post(uri"$embeddingUrl")
        .auth.bearer(token)
        .header("Content-Type", "application/json")
        .body(jsonPayload)
        .send())

      _ <- if (response.code == StatusCode.Ok) IO.unit
      else IO.raiseError(new Exception(s"API request failed with status: ${response.statusText}"))

      responseStr <- IO.pure(response.body)
      parsed <- IO.fromEither(decode[EmbeddingResponse](responseStr).left.map(
        err => new Exception(s"Failed to parse response: $err"))
      )
      embedding <- IO.fromEither(parsed.data.headOption.map(_.embedding).toRight(
        new Exception("No embeddings found"))
      )

//      embedding <- IO.fromEither(
//        decode[Array[Double]](response.body)
//          .left.map(err => new Exception(s"Failed to parse response: $err"))
//      )
    } yield embedding
  }


}
