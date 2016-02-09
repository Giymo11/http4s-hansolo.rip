package rip.hansolo.http4s.service

import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`

import scalatags.Text.all._
import scalatags.Text.tags2
/**
  * Created by Giymo11 on 09.02.2016.
  */
object ScalaJsAppService {
  def apply(title: String, sjsMainObject: String): HttpService = HttpService {
    case req @ GET -> Root =>
      Ok(
        html(
          head(
            tags2.title(title),
            meta(httpEquiv := "Content-Type", content := "text/html", charset := "UTF-8")
          ),
          body(margin := 0),
          script(`type` := "text/javascript", src := "/public/scala-js-fastopt.js"),
          script(sjsMainObject + "().main()")
        ).render
      ).withContentType(Some(`Content-Type`(`text/html`)))
  }
}
