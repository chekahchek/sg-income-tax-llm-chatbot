package ingestion

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

    websites.traverse { site =>
      for {
        contents <- scrapeWebsite(site)
        embedding <- getEmbeddings(contents)
        _ <- transactor.use {xa =>
          insertTextAndEmbeddings(contents, embedding).run.transact(xa)
        }
      } yield ()
    }.as(ExitCode.Success)
  }

}