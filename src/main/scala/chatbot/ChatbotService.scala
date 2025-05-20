package chatbot

import cats.effect.{ExitCode, IOApp, IO}
import api.LLMClient.getEmbeddings
import database.DbConfig
import database.DbService.createDbSession
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import sql.Queries.reteiveContext
import doobie.implicits._


object ChatbotService extends IOApp{

  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor = createDbSession(config)

    val query = "How much is the earned income relief?"
    for {
      embeddings <- getEmbeddings(query)
      embeddingStr = embeddings.mkString("[", ",", "]")
      context <-  transactor.use { xa => reteiveContext(embeddingStr).transact(xa)}
      _ <- IO.println(context)
    } yield ()
  }.as(ExitCode.Success)
}
