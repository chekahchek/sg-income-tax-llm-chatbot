package sql

import java.time.Instant
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

object Queries {
  def insertTextAndEmbeddings(title: String,
                              content: String,
                              embedding: Array[Double],
                              ts : Instant
                             ): Update0 = {
    sql"""
         INSERT INTO documents (title, content, embedding, extract_time)
         VALUES ($title, $content, $embedding, $ts)
       """.update
  }

  def retrieveContext(embeddingStr: String, limit: Int = 1): ConnectionIO[List[String]] = {
    sql"""
  SELECT content
  FROM documents
  ORDER BY 1 - (embedding <=> $embeddingStr::vector) DESC
  LIMIT $limit
  """.query[String].to[List]
  }


}
