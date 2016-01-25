package rip.hansolo.http4s.service

import _root_.argonaut._
import org.http4s.dsl._
import org.http4s.argonaut._
import org.http4s._

import scalaz._, Scalaz._
import argonaut._

/**
  * Created by Giymo11 on 24.01.2016.
  */
object GithubWebhookService {

  def apply(): HttpService = service

  private val service = HttpService {
    case req @ POST -> Root =>

      val master = for {
        // turn the body into Json, the error into a String
        payload <- req.attemptAs[Json].run.run.leftMap(_.toString)
        // read the field "ref", get rid of the '"'
        ref <- payload.field("ref").map(_.toString().replace("\"", "")) \/> "Field 'ref' not found"
        // check if its the right branch
        master <- (ref === "refs/heads/master").option() \/> "Not the master branch"
      } yield master

      master match {
        case -\/(error) => println("error: " + error)
        case \/-(_) =>
          import sys.process._
          println("git pull origin master" !)
      }

      Accepted()
  }

}
