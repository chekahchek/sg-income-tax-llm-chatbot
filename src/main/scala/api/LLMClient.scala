package api

import sttp.client4.Response
import sttp.client4.quick._
import sttp.model.StatusCode
import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._


object LLMClient {
  // Used to decode embedding response
  case class EmbeddingData(embedding: Array[Double])
  case class EmbeddingResponse(data: List[EmbeddingData])

  // Used to decode chat response
  case class ChatOutput(text: String)
  case class ChatContent(content: List[ChatOutput])
  case class ChatResponse(output: List[ChatContent])

  private val token = sys.env.get("API_KEY") match {
    case Some(key) if key.nonEmpty => key
    case _ => throw new Exception("API_KEY environment variable not set")
  }

  private val embeddingModel = "text-embedding-3-small"
  private val chatModel = "gpt-4.1-nano"
  private val embeddingUrl = "https://api.openai.com/v1/embeddings"
  private val chatUrl = "https://api.openai.com/v1/responses"


  private def decodeOpenAIEmbed(response: Response[String]): Either[Throwable, Array[Double]] = {
    for {
      parsed <- decode[EmbeddingResponse](response.body)
      embedding <- parsed.data.headOption.map(_.embedding).toRight(
        new Exception("No embeddings found")
      )
    } yield embedding
  }

  private def decodeOpenAIChat(response: Response[String]): Either[Throwable, String] = {
    for {
      parsed <- decode[ChatResponse](response.body)
      chatOutput <- parsed.output.headOption.map(_.content.headOption.map(_.text).getOrElse("")).toRight(
        new Exception("No chat output found")
      )
    } yield chatOutput
  }

  def getEmbeddings(text: String): IO[Array[Double]] = {
    val jsonPayLoad = Map("input" -> text, "model" -> embeddingModel).asJson.noSpaces
    for {
      response <- IO(quickRequest
        .post(uri"$embeddingUrl")
        .auth.bearer(token)
        .header("Content-Type", "application/json")
        .body(jsonPayLoad)
        .send())

      _ <- if (response.code == StatusCode.Ok) IO.unit
      else IO.raiseError(new Exception(s"API request failed with status: ${response.statusText}"))

      embedding <- IO.fromEither(decodeOpenAIEmbed(response))
    } yield embedding
  }

  def generateChatResponse(query: String, instructions: String): IO[String] = {
    val jsonPayLoad = Map("input" -> query, "instructions" -> instructions, "model" -> chatModel).asJson.noSpaces
    for {
      response <- IO(quickRequest
        .post(uri"$chatUrl")
        .auth.bearer(token)
        .header("Content-Type", "application/json")
        .body(jsonPayLoad)
        .send())

      _ <- if (response.code == StatusCode.Ok) IO.unit
      else IO.raiseError(new Exception(s"API request failed with status: ${response.statusText}"))
      chatOutput <- IO.fromEither(decodeOpenAIChat(response))
    } yield chatOutput
  }
}
