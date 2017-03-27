name := "hail-tests"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  // tests
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",

  //sangria
  "org.sangria-graphql" %% "sangria" % "1.1.0",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1",

  "org.scalaj" %% "scalaj-http" % "2.3.0",
  // "org.json4s" %% "json4s-jackson" % "3.3.0",

  "org.apache.spark" %% "spark-core" % "2.0.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.0.2" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.0.2" % "provided"
)

test in assembly := {}

// unmanagedBase := file("/Users/msolomon/src/spark-2.0.2-bin-hadoop2.7/jars")
unmanagedBase := file("/Users/msolomon/Projects/scala-stuff/hail-tests/lib")

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
