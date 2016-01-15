package rip.hansolo.http4s

import rip.hansolo.http4s.service._
import rip.hansolo.http4s.service.special._

import org.http4s.server.blaze.BlazeBuilder

object Api extends App {

  // the 0.0.0.0 enables it to be picked up from outside
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