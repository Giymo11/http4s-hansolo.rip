package rip.hansolo.http4s.service

import org.http4s.MediaType._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server._

import scalacss.Defaults._
import scalacss.ScalatagsCss._
import scalatags.Text._
import scalatags.Text.short._
import scalatags.Text.tags._

object MainPageService {

  def apply(): HttpService = service

  object MyStyles extends StyleSheet.Inline {

    import dsl._

    // typesafe css, with autocompletion!
    val centerContainer = style(
      display.flex,
      alignItems.center,
      justifyContent.center
    )
  }

  import MyStyles._

  private val htmlContentType = Some(`Content-Type`(`text/html`))

  private val service = HttpService {
    case request @ GET -> Root =>
      Ok(
        html(
          // render the stylesheet once
          MyStyles.render[TypedTag[String]],

          // this is needed not to confuse it with the Attr title
          head(tags2.title("RIP Han Solo :(")),

          body(
            // and then apply it
            centerContainer,
            img(*.src := "http://i.imgur.com/lvuZhX4.jpg")
          )
        ).render
      ).withContentType(htmlContentType)
  }
}
