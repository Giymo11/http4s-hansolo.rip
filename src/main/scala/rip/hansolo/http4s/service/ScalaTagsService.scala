package rip.hansolo.http4s.service

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import org.http4s.headers.{`Content-Type`, `Content-Length`}
import org.http4s._
import org.http4s.MediaType._
import org.http4s.dsl._
import org.http4s.argonaut._
import org.http4s.server._
import org.http4s.server.middleware.PushSupport._
import org.http4s.server.middleware.authentication._

import scalacss.ValueT
import scalaz.stream.Process
import scalaz.stream.time
import scalaz.concurrent.Task
import scalaz.concurrent.Strategy.DefaultTimeoutScheduler

import scalatags._
import scalatags.Text._
import scalatags.Text.short._
import scalatags.Text.tags._

import scalacss.Defaults._
import scalacss.ScalatagsCss._

import _root_.argonaut._
import Argonaut._

object ScalaTagsService {

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
          head(tags2.title("hansolo.rip")),

          body(
            // and then apply it
            centerContainer,
            img(*.src := "http://i.imgur.com/lvuZhX4.jpg")
          )
        ).render
      ).withContentType(htmlContentType)
  }
}
