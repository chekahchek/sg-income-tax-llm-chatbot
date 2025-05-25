package ingestion

import api.LLMClient._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import database.DbConfig
import database.DbService.createDbSession
import doobie.implicits._
import ingestion.WebScrape._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import sql.Queries._

import java.time.Instant

object Vectorisation extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor       = createDbSession(config)

    articles
      .traverse { article =>
        for {
          contents  <- scrapeWebsite(rootURL, article)
          embedding <- getEmbeddings(contents)
          _ <- transactor.use { xa =>
            insertTextAndEmbeddings(article, contents, embedding, Instant.now()).run.transact(xa)
          }
        } yield ()
      }
      .as(ExitCode.Success)
  }

}
