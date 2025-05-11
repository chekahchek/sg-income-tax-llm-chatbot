ThisBuild / scalaVersion     := "2.13.16"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val CirceVersion = "0.14.0-M5"
val Http4sVersion = "1.0.0-M21"
val DoobieVersion = "1.0.0-RC8"

lazy val root = (project in file("."))
  .settings(
    name := "sg-income-tax-llm-chatbot",
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "cats-effect"         % "3.3.14",
      "org.jsoup"        % "jsoup"               % "1.18.3",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-RC1",
      "com.lihaoyi" %% "upickle" % "4.1.0",
      "org.tpolecat"    %% "skunk-core"          % "0.3.1",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.5",
      "com.github.pureconfig" %% "pureconfig-generic" % "0.17.5",
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "io.circe"        %% "circe-literal"       % CirceVersion,
      "io.circe"        %% "circe-parser"        % CirceVersion,
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.scalatest" %% "scalatest" % "3.2.19",
      "org.tpolecat" %% "doobie-core"      % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"          % DoobieVersion,

    )
  )