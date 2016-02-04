package rip.hansolo.http4s.service

import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import scalatags.Text._

import scalatags.Text.all._

/**
  * Created by Giymo11 on 2016-02-04 at 01:33.
  */
object GameService {

  def apply(): HttpService = service

  private val service = HttpService {
    case req @ GET -> Root =>
      Ok(
        html(
          head(
            tags2.title("Dodge the Dots"),
            meta(httpEquiv := "Content-Type", content := "text/html", charset := "UTF-8")
          ),
          body(
            margin := 0,
            div(
              id := "playground",
              canvas(
                id := "canvas",
                display.block
              )
            ),
            script(`type` := "text/javascript", src := "/public/scala-js-fastopt.js"),
            script("GameScript().main()")
          )
        ).render
      ).withContentType(Some(`Content-Type`(`text/html`)))
  }
}
