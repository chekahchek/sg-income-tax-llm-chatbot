package chatbot

import cats.effect.{IO, Resource}
import api.LLMClient.getEmbeddings
import doobie.hikari.HikariTransactor
import sql.Queries.retrieveContext
import doobie.implicits._
import api.LLMClient.generateChatResponse
import PromptTemplate.prompt


class ChatbotService(transactor: Resource[IO, HikariTransactor[IO]]) {

  def chat(query: String): IO[String] = {
    for {
      embeddings <- getEmbeddings(query)
      embeddingStr = embeddings.mkString("[", ",", "]")
      context <- transactor.use { xa => retrieveContext(embeddingStr).transact(xa) }
      queryWithContext = query + "\n" + "###\n" + context.mkString("\n") + "\n###"
      chatResponse <- generateChatResponse(queryWithContext, prompt)
    } yield chatResponse
  }
}
