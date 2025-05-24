package chatbot

object PromptTemplate {
  val prompt =
    """Your role is to answer the user's question on income tax matters in Singapore.
      |Use the relevant context delimited in triple hashtag (i.e. ###) to answer the question.""".stripMargin
}
