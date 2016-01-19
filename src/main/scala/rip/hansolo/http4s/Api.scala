package rip.hansolo.http4s

import org.http4s.Response
import org.http4s.server._
import rip.hansolo.http4s.service._
import rip.hansolo.http4s.service.special._

import org.http4s.server.blaze.BlazeBuilder

import scalaz.concurrent.Task

object Api extends App {
  import org.http4s.dsl._
  val lifted = HttpService.lift { req => Task.now{
    println(req.uri.authority)
    Response(Ok)}
  }



  // the 0.0.0.0 enables it to be picked up from outside
  BlazeBuilder.bindHttp(80, "0.0.0.0")
    .mountService(ScalaTagsService(), "/")
    .mountService(UserService(), "/users")
    .mountService(ProductService(), "/products")
    .mountService(StreamingService(), "/streaming")
    .mountService(WsService(), "/ws")
    .mountService(TellMeService(), "/info")
    .mountService(HomeService(), "/home")
    .mountService(lifted, "/lifted")
    .run
    .awaitShutdown()

}
