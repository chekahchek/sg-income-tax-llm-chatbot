package chatbot

import org.jsoup._
import collection.JavaConverters._

object WebScrape {

  val websites : List[String] = List(
//    "https://en.wikipedia.org/"
    "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/tax-reliefs-rebates-and-deductions/tax-reliefs/earned-income-relief"
  )

  def main(args: Array[String]): Unit = {
    for (site <- websites) {
      val doc = Jsoup.connect(site).get()
      val sections = doc.select("section").asScala
      val excludedContents = List("FAQs", "Related Content")
      val results = sections.flatMap { section =>
          val h2 = section.select("h2")
          if (h2.attr("id").startsWith("title") &&
            !excludedContents.exists(exclude => h2.text().contains(exclude)))
          Some(section.text())
          else None
      }
      results.foreach(println)
      }
    }

}
