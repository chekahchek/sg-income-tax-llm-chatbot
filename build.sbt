val CatsEffectVersion =

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val CirceVersion = "0.14.0-M5"

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

    )
  )