package rip.hansolo.http4s.service

import org.http4s._

import scalatags.Text.all._
/**
  * Created by Giymo11 on 09.02.2016.
  */
object ScalaJsAppService {
  def apply(title: String, sjsMainObject: String, useMdl: Boolean = false): HttpService =
    ScalatagsService(
      title,
      Seq(body(margin := 0),
        script(`type` := "text/javascript", src := "/public/scala-js-fastopt.js"),
        script(sjsMainObject + "().main()")),
      useMdl
    )
}
