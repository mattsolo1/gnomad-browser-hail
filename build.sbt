import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "programminginscala",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    libraryDependencies ++= Seq(
      //sangria
      "org.sangria-graphql" %% "sangria" % "1.1.0",
      "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
      "com.typesafe.akka" %% "akka-http" % "10.0.1",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1",

      // requests
      "org.scalaj" %% "scalaj-http" % "2.3.0",

      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )
