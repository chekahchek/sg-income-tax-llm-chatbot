package sql

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

object Queries {

  def insertTextAndEmbeddings(id: String, embedding: Array[Double]): Update0 = {
    sql"""
         INSERT INTO tax (id, embedding)
         VALUES ($id, $embedding)
       """.update
  }

  def compareEmbeddings(embeddingStr: String, limit: Int = 5): ConnectionIO[List[String]] = {
    sql"""
  SELECT id
  FROM embeddings
  ORDER BY 1 - (embedding <=> $embeddingStr::vector) DESC
  LIMIT $limit
  """.query[String].to[List]
  }


}
