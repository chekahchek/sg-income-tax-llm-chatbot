package api

import sttp.client4.Response
import sttp.client4.quick._
import sttp.model.StatusCode
import cats.effect.IO
import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser._


object LLMClient {

  case class EmbeddingData(embedding: Array[Double])
  case class EmbeddingResponse(data: List[EmbeddingData])


  private val embeddingModel = "text-embedding-3-small"
  private val embeddingUrl = "https://api.openai.com/v1/embeddings"

  private def decodeOpenAIEmbed(response: Response[String]): Either[Throwable, Array[Double]] = {
  for {
    parsed <- decode[EmbeddingResponse](response.body)
    embedding <- parsed.data.headOption.map(_.embedding).toRight(
      new Exception("No embeddings found")
    )
  } yield embedding
  }

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

      embedding <- IO.fromEither(decodeOpenAIEmbed(response))
    } yield embedding
  }
}
