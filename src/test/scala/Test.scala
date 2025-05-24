import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp, Resource}
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import pureconfig.generic.auto._
import doobie.util.transactor.Transactor
import org.scalatest.funsuite.AnyFunSuite
import ingestion.WebScrape._
import api.LLMClient._
import database.DbConfig
import database.DbService.createDbSession
import pureconfig.ConfigSource
import chatbot.ChatbotService

class Test extends AnyFunSuite {
  test("Insert Embeddings") {
    // Create a transactor - this is equivalent to the Skunk Session
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = "jdbc:postgresql://localhost:5432/postgres",
      user = "postgres",
      password = "postgres",
      logHandler = None,
    )

    // Define the insert SQL
    def insert(id: String, embedding: Array[Double]): Update0 =
      sql"""
         INSERT INTO embeddings (id, embedding)
         VALUES ($id, $embedding)
       """.update

    // Execute the query
    val program: IO[Int] = {
      val id = "doc1"
      val embedding = Array(0.1, 0.2, 0.3) // Make sure this matches VECTOR(n)

      sql"INSERT INTO embeddings (id, embedding) VALUES ($id, $embedding)"
        .update
        .run
        .transact(xa)
    }

    // Run the program
    program.unsafeRunSync()
  }

  test("Webscrape") {
    val rootUrl = "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/"
    val article = "parent-relief-parent-relief-(disability)"
    val result = scrapeWebsite(rootUrl, article).unsafeRunSync()
    println(result)
  }

  test("Chat") {
    val query = "hi\nhow are you?"
    val instructions = "You are a helpful assistant."
    val result = generateChatResponse(query, instructions).unsafeRunSync()
    println(result)
  }

  test("Chat with context") {
    val query = "How much is the earned income relief?"
    val config: DbConfig = ConfigSource.default.at("db").loadOrThrow[DbConfig]
    val transactor = createDbSession(config)
    val chatbotService = new ChatbotService(transactor)
    val response = chatbotService.chat(query).unsafeRunSync()
    println(response)
  }
}