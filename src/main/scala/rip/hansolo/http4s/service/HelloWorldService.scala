package rip.hansolo.http4s.service

import org.http4s.dsl._
import org.http4s.server.HttpService

object HelloWorldService {

  def apply(): HttpService = service

  private val service = HttpService {
    case GET -> Root =>
      Ok("Hello World!!!")
  }

}
