package ingestion

import cats.effect.IO
import org.jsoup._
import scala.jdk.CollectionConverters._

object WebScrape  {
  // Sites to scrape
  val excludedContents = List("FAQs", "Related Content")
  val rootURL = "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/"
  val articles : List[String] = List(
    "earned-income-relief",
    "spouse-relief-spouse-relief-(disability)",
    "foreign-domestic-worker-levy-(fdwl)-relief",
    "central-provident-fund(cpf)-relief-for-employees",
    "central-provident-fund-(cpf)-relief-for-self-employed-employee-who-is-also-self-employed",
    "parent-relief-parent-relief-(disability)",
    "grandparent-caregiver-relief",
    "sibling-relief-(disability)",
    "working-mother's-child-relief-(wmcr)",
    "qualifying-child-relief-(qcr)-child-relief-(disability)",
    "life-insurance-relief",
    "course-fees-relief",
    "central-provident-fund-(cpf)-cash-top-up-relief",
    "compulsory-and-voluntary-medisave-contributions"
  )

  def scrapeWebsite(rootURL: String, article: String): IO[String] = {
      val doc = Jsoup.connect(rootURL.concat(article))
        .timeout(10000)
        .userAgent("Mozilla/5.0")
        .get()

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