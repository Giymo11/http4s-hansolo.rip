package rip.hansolo.http4s.service

import org.http4s.dsl._
import org.http4s.server.HttpService

/**
  * Created by Giymo11 on 2016-01-13 at 12:37.
  */
object TellMeService {

  def apply(): HttpService = service

  private val service = HttpService {
    case req @ GET -> Root / "authority" => {
      println(req.uri.authority)
      Ok("" + req.uri.authority)
    }

    case req @ GET -> Root / "params" => {
      println(req.uri.params)
      Ok("" + req.uri.params)
    }

    case req @ GET -> Root / "raw" => {
      val request = req
      println("remoteHost: " + req.remoteHost.get)
      println("pathInfo: " + req.pathInfo)
      println("serverAddr: " + req.serverAddr)
      println("queryString: " + req.queryString)

      println(req.multiParams.iterator.map(x => x._1 + ": " + x._2.fold("")(_ + " " + _)))

      val attributes = req.attributes
      for(i <- attributes.keys) {
        val value = attributes.get(i)
        println("" + i + ": " + value)
      }
      Ok()
    }

    case req @ GET -> Root / "uri" => {
      val uri = req.uri
      println("host: " + uri.host.get)
      uri.fragment.foreach(println)
      println(uri.query.toVector.map((tuple) => "" + tuple._1 + ": " + tuple._2.getOrElse("")))
      uri.host.foreach(println)
      Ok()
    }


  }
}
