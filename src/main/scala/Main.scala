import cats.effect._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import database.DbConfig
import database.DbService.createDbSession
import chatbot.ChatbotService
object Main extends IOApp {

  import scala.concurrent.ExecutionContext.global

  def runServer: Resource[IO, ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val session  = createDbSession(config)
    val repository = new ChatbotService(session)
    for {
      _ <- BlazeServerBuilder[IO](global)
        .bindHttp(8080, "localhost")
        .withHttpApp(new Routes(repository).routes.orNotFound)
        .resource
    } yield ExitCode.Success
  }


  override def run(args: List[String]): IO[ExitCode] =
    runServer.use(_ => IO.never).as(ExitCode.Success)
}