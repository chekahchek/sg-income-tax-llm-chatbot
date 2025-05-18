package chatbot

import cats.effect.{ExitCode, IOApp, IO}
import api.LLMClient.getEmbeddings
import database.DbConfig
import database.DbService.createDbSession
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import sql.Queries.compareEmbeddings
import doobie.implicits._


object ChatbotService extends IOApp{

  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor = createDbSession(config)

    val query = "Test"
//    val embeddings = Array(0.1, 0.2, 0.3)

    for {
      embeddings <- getEmbeddings(query)
      embeddingStr = embeddings.mkString("[", ",", "]")
      context <-  transactor.use { xa => compareEmbeddings(embeddingStr).transact(xa)}
      _ <- IO.println(context)
    } yield ()
  }.as(ExitCode.Success)
}
