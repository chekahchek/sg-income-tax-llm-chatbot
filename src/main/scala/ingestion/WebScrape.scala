package ingestion

import cats.effect.IO
import org.jsoup._
import scala.collection.JavaConverters._
//import scala.annotation.unused
//import java.io.File


object WebScrape  {
  // Sites to scrape
  val excludedContents = List("FAQs", "Related Content")
  val websites : List[String] = List(
    "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/earned-income-relief"
  )

  def scrapeWebsite(site: String): IO[String] = {
      val doc = Jsoup.connect(site)
        .timeout(10000)
        .userAgent("Mozilla/5.0")
        .get()

//      val file = new File("src/main/scala/chatbot/IRAS.html")
//      val doc = Jsoup.parse(file)

      val resultsBuffer = doc.select("section").asScala.flatMap { section =>
        val h2 = section.select("h2")
        if (h2.attr("id").startsWith("title") &&
          !excludedContents.exists(exclude => h2.text().contains(exclude)))
          Some(section.text())
        else None
      }

      val results = resultsBuffer.mkString("\n")
      if (results.isEmpty) {
        IO.raiseError(new Exception(s"Content is empty"))
      } else {
        IO.pure(results)
      }
    }

}