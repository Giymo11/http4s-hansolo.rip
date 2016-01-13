package com.gvolpe.api.service

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


  }
}
