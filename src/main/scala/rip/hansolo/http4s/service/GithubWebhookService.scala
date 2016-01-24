package rip.hansolo.http4s.service

import org.http4s.dsl._
import org.http4s.argonaut._
import org.http4s.MediaType._
import org.http4s.server._

import scalaz._, Scalaz._
import argonaut._, Argonaut._

/**
  * Created by Giymo11 on 24.01.2016.
  */
object GithubWebhookService {

  def apply(): HttpService = service

  private val service = HttpService {
    case req @ POST -> Root =>

      val payload = req.attemptAs[Json].run.run.leftMap(_.toString)

      payload match {
        case -\/(error) => println("error: " + error)
        case \/-(jsonPayload) => println("json: " + jsonPayload.spaces2)
      }

      val master: \/[String, Boolean] =
        payload.flatMap(_.field("ref").map(_.string).map(_.contains("master")) \/> "Branch is not master")

      master match {
        case -\/(error) => println("error: " + error)
        case \/-(isMaster) => println("isMaster: " + isMaster)
      }

      Accepted()
  }

}
