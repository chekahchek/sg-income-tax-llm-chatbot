package sql

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

object Queries {
  def insertTextAndEmbeddings(content: String, embedding: Array[Double]): Update0 = {
    sql"""
         INSERT INTO documents (content, embedding)
         VALUES ($content, $embedding)
       """.update
  }

  def compareEmbeddings(embeddingStr: String, limit: Int = 5): ConnectionIO[List[String]] = {
    sql"""
  SELECT content
  FROM documents
  ORDER BY 1 - (embedding <=> $embeddingStr::vector) DESC
  LIMIT $limit
  """.query[String].to[List]
  }


}
