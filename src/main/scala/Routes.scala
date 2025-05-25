import cats.effect.IO
import chatbot.ChatbotService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class Routes(chatbot: ChatbotService) extends Http4sDsl[IO] {

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")

    case req @ POST -> Root / "chat" =>
      for {
        query  <- req.as[String]
        result <- chatbot.chat(query).attempt
        response <- result match {
          case Right(chatResponse) => Ok(chatResponse)
          case Left(_)             => BadRequest()
        }
      } yield response
  }
}
