package rip.hansolo.http4s.service

import org.http4s.dsl._
import org.http4s.server.HttpService


/**
  * Created by Giymo11 on 2016-01-13 at 12:37.
  */
object TellMeService {

  def apply(): HttpService = HttpService {
    case req @ GET -> Root / "authority" =>
      val uri = req.uri

      println("uri host: " + uri.host)
      println("uri host: " + uri.host.map(_.value))

      val authority = uri.authority
      val host = authority.map(_.host)
      val userInfo = authority.flatMap(_.userInfo).map(_.toString)

      println("authority: " + authority)
      println("host: " + host)
      println("userInfo: " + userInfo)

      Ok()

    case req @ GET -> Root / "attributes" =>
      println("remoteHost: " + req.remoteHost.get)
      println("serverAddr: " + req.serverAddr)
      println("toString: " + req.toString)

      println("attributes: ")
      val attributes = req.attributes
      for(key <- attributes.keys) {
        val value = attributes.get(key)
        println("" + key + ": " + value)
      }

      Ok()
  }
}
