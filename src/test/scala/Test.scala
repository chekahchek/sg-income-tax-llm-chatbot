import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp, Resource}
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.funsuite.AnyFunSuite
import ingestion.WebScrape._

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
    val site = "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/parent-relief-parent-relief-(disability)"
    val result = scrapeWebsite(site).unsafeRunSync()
    println(result)
  }
}