package dataclass


final case class DbConfig(driver: String,
                          url: String,
                          user: String,
                          password: String,
                          threadPoolSize: Int
                         )
