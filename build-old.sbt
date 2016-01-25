name := """simple-http4s-api"""

version := "1.0"

scalaVersion := "2.11.7"

val http4sVersion = "0.12.0"
val circeVersion = "0.2.1"
val scalazCoreVersion = "7.1.4"
val specs2Version = "3.7"
// cannot bump to scalaz 7.2.0 yet
val scalaCssVersion = "0.3.1"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "org.scalaz" %% "scalaz-core" % scalazCoreVersion,
  "com.chuusai" %% "shapeless" % "2.2.5",
  "com.lihaoyi" %% "scalatags" % "0.5.4",
  "com.github.japgolly.scalacss" %% "core" % scalaCssVersion,
  "com.github.japgolly.scalacss" %% "ext-scalatags" % scalaCssVersion,
  "com.typesafe.play" % "play-json_2.11" % "2.4.6"
)

// most of the libraries taken from the http4s project

// no tests for now, they will follow after the scala-js integration
/*libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % specs2Version,
  "org.scalacheck" %% "scalacheck" % "1.12.5",
  // maybe later logbackClassic
  "org.scalaz" %% "scalaz-scalacheck-binding" % scalazCoreVersion,
  "org.specs2" %% "specs2-matcher-extra" % specs2Version,
  "org.specs2" %% "specs2-scalacheck" % specs2Version,
  "org.scalatest" %% "scalatest" % "2.2.6"
).map(_ % "test")*/

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*Api.*;.*WsService.*"
