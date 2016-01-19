package rip.hansolo.http4s.service

import org.http4s.{Response, Request, HeaderKey}
import org.http4s.dsl._
import org.http4s.headers.Host
import org.http4s.server.HttpService

import scalaz.concurrent.Task

/**
  * Created by Giymo11 on 2016-01-13 at 12:37.
  */
object TellMeService {

  // probably should do this with middleware
  // https://github.com/http4s/http4s/blob/master/docs/src/test/scala/org/http4s/docs/CompositionExample.scala
  def apply(): HttpService = HttpService {
    case req @ GET -> Root / _ => {
      println("hello")
      val uriHost = req.uri.host.map(_.value)
      val headerHost = req.headers.get(Host).map(_.value)
      println(uriHost)
      println(headerHost)
      val hostString = uriHost.orElse(headerHost)
      if (hostString.isDefined &&
         (hostString.get.contains("test") || hostString.get.contains("localhost")) ) {
        println("handling")
        handle(req)
      } else {
        println("not found: " + hostString)
        NotFound()
      }
    }
  }

  private def handle: PartialFunction[Request, Task[Response]] = {
    case req @ GET -> Root / "authority" => {

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
    }


    case req @ GET -> Root / "attributes" => {

      println("remoteHost: " + req.remoteHost.get)
      println("serverAddr: " + req.serverAddr)
      println("toString: " + req.toString)

      println("attributes: ")
      val attributes = req.attributes
      for(key <- attributes.keys) {
        val value = attributes.get(key)
        println("" + key + ": " + value)
      }

      import org.http4s.headers.Host
      val hostString = req.headers.get(Host).map(_.value)
      println("host: " + hostString)

      Ok(hostString.getOrElse(""))
    }


  }
}
