package com.gvolpe.api.service

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

import scalaz.stream.Process
import scalaz.stream.time
import scalaz.concurrent.Task
import scalaz.concurrent.Strategy.DefaultTimeoutScheduler

import scalatags._
import scalatags.Text._
import scalatags.Text.all._

import _root_.argonaut._
import Argonaut._

/**
  * Created by Giymo11 on 2016-01-13 at 13:00.
  */
object ScalaTagsService {

  def apply(): HttpService = service

  private val service = HttpService {
    case req @ GET -> Root => {
      Ok(
        html(
          head(
            title := "hansolo.rip"
          ),
          body(
            img(attrs.src := "http://i.imgur.com/lvuZhX4.jpg")
          )
        ).render
      ).withContentType(Some(`Content-Type`(`text/html`)))
    }
  }
}
