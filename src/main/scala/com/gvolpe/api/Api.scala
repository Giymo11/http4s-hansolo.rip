package com.gvolpe.api

import com.gvolpe.api.service._
import com.gvolpe.api.service.special.{WsService, StreamingService}
import org.http4s.server.blaze.BlazeBuilder

object Api extends App {

  BlazeBuilder.bindHttp(80, "0.0.0.0")
    .mountService(ScalaTagsService(), "/")
    .mountService(UserService(), "/users")
    .mountService(ProductService(), "/products")
    .mountService(StreamingService(), "/streaming")
    .mountService(WsService(), "/ws")
    .mountService(TellMeService(), "/info")
    .mountService(HomeService(), "/home")
    .run
    .awaitShutdown()

}
