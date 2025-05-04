package dataclass

final case class DbConfig(host: String,
                          port: Int,
                          username: String,
                          password: String,
                          database: String,
                          sessions: Int)