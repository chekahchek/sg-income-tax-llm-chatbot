package chatbot

import api.LLMClient.{generateChatResponse, getEmbeddings}
import cats.effect.{IO, Resource}
import chatbot.PromptTemplate.prompt
import doobie.hikari.HikariTransactor
import doobie.implicits._
import sql.Queries.retrieveContext

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
