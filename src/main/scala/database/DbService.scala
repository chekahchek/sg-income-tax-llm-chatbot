package database

import cats.effect.{IO, Resource}
import dataclass.DbConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object DbService {
  def transactor(config: DbConfig, executionContext: ExecutionContext): Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext
      )
  }

  def createDbSession(config: DbConfig): Resource[IO, HikariTransactor[IO]] = {
    for {
      config <- Resource.pure(config)
      ec <- ExecutionContexts.fixedThreadPool[IO](config.threadPoolSize)
      transactor <- transactor(config, ec)
    } yield transactor
  }
}