package chatbot
import cats.effect.{IO, Resource}
import skunk.Session
import natchez.Trace
import natchez.Trace.Implicits.noop
import dataclass.DbConfig

object Database {
  def pooledSession(config: DbConfig)(implicit trace: Trace[IO]): Resource[IO, Resource[IO, Session[IO]]] = {
    Session.pooled[IO](
      host = config.host,
      port = config.port,
      user = config.username,
      password = Some(config.password),
      database = config.database,
      max = config.sessions
    )
  }

  def createDbSession(config: DbConfig): Resource[IO, Resource[IO, Session[IO]]] = {
    for {
      session <- Database.pooledSession(config)
    } yield session
  }
}