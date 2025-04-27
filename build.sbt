val CatsEffectVersion =

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "sg-income-tax-llm-chatbot",
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "cats-effect"         % "3.3.14",
      "org.jsoup"        % "jsoup"               % "1.18.3",
    )
  )