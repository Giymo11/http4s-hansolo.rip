package rip.hansolo.http4s.service

import PlayJsonImplicits._
import org.http4s.dsl._
import org.http4s.server.HttpService
import play.api.libs.json.Json

object ProductService {

  def apply(): HttpService = service

  private val service = HttpService {
    case GET -> Root =>
      val products = List(Product(1, "Book"), Product(2, "Calc"), Product(3, "Guitar"))
      Ok(Json.toJson(products))
    case GET -> Root / id =>
      Ok(Json.toJson(Product(id.toLong, s"Name#$id")))
  }

}
