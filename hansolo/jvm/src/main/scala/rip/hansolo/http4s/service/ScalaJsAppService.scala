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
  def apply(title: String, sjsMainObject: String, useMdl: Boolean = false): HttpService = HttpService {
    case req @ GET -> Root =>
      Ok(
        html(
          head(
            if(useMdl) Seq[Frag](
              link(rel := "stylesheet", href := "https://fonts.googleapis.com/icon?family=Material+Icons"),
              link(rel := "stylesheet", href := "https://code.getmdl.io/1.1.1/material.indigo-pink.min.css"),
              link(rel := "stylesheet", href := "http://fonts.googleapis.com/css?family=Roboto:300,400,500,700"),
              script(src := "https://code.getmdl.io/1.1.1/material.min.js", "defer".attr := true)
            ) else Seq[Frag](),
            tags2.title(title),
            meta(httpEquiv := "Content-Type", content := "text/html", charset := "UTF-8"),
            meta(name := "viewport", content := "width=device-width, initial-scale=1.0")
          ),
          body(margin := 0),
          script(`type` := "text/javascript", src := "/public/scala-js-fastopt.js"),
          script(sjsMainObject + "().main()")
        ).render
      ).withContentType(Some(`Content-Type`(`text/html`)))
  }
}
