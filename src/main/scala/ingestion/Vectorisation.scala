package ingestion

import java.time.Instant
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import cats.effect.{IO, IOApp, ExitCode}
import cats.implicits._
import WebScrape._
import api.LLMClient._
import database.DbConfig
import sql.Queries._
import database.DbService.createDbSession

object Vectorisation extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor = createDbSession(config)

    articles.traverse {article =>
      for {
        contents <- scrapeWebsite(rootURL, article)
        embedding <- getEmbeddings(contents)
        _ <- transactor.use {xa =>
          insertTextAndEmbeddings(article, contents, embedding, Instant.now()).run.transact(xa)
        }
      } yield ()
    }.as(ExitCode.Success)
  }

}