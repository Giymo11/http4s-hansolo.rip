// Turn this project into a Scala.js project by importing these settings

import sbt.Keys._
import com.lihaoyi.workbench.Plugin._
import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.sonatypeRepo("snapshots")

val http4sVersion = "0.12.1"
val circeVersion = "0.2.1"
val scalazCoreVersion = "7.1.4"
val specs2Version = "3.7"
// cannot bump to scalaz 7.2.0 yet
val scalaCssVersion = "0.3.1"

val hansolo = crossProject.settings(
  name := "hansolo.rip",
  scalaVersion := "2.11.7",
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.3.8",
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "scalatags" % "0.5.4",
    "com.chuusai" %%% "shapeless" % "2.2.5"
  )
).jsSettings(
  workbenchSettings:_*
).jsSettings(
  name := "scala-js",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.2",
    "com.timushev" %%% "scalatags-rx" % "0.1.0",
    "com.lihaoyi" %%% "scalarx" % "0.3.0"
  ),
  bootSnippet := "rip.hansolo.script.GameScript().main();",
  updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
).jvmSettings(
  Revolver.settings:_*
).jvmSettings(
  name := "http4s-server",
  libraryDependencies ++= Seq(
    "io.spray" %% "spray-can" % "1.3.3",
    "io.spray" %% "spray-routing" % "1.3.3",
    "com.typesafe.akka" %% "akka-actor" % "2.4.1",
    "org.webjars" % "bootstrap" % "3.3.6",
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-argonaut" % http4sVersion,
    "org.scalaz" %% "scalaz-core" % scalazCoreVersion,
    "com.github.japgolly.scalacss" %% "core" % scalaCssVersion,
    "com.github.japgolly.scalacss" %% "ext-scalatags" % scalaCssVersion,
    "com.typesafe.play" % "play-json_2.11" % "2.4.6"
  )
)

val `hansolo-js` = hansolo.js
val `hansolo-jvm` = hansolo.jvm.settings(
  (resources in Compile) += {
    (fastOptJS in (`hansolo-js`, Compile)).value
    (artifactPath in (`hansolo-js`, Compile, fastOptJS)).value
  }
)
