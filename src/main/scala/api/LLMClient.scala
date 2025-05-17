package api


import sttp.client4.quick._
import sttp.model.StatusCode
import cats.effect.IO
import cats.implicits._
import io.circe.parser._


object LLMClient {
  // HuggingFace models for tokenization
  private val embeddingModel = "sentence-transformers/all-MiniLM-L6-v2"
  private val embeddingUrl = s"https://api-inference.huggingface.co/pipeline/feature-extraction/$embeddingModel"


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
}
