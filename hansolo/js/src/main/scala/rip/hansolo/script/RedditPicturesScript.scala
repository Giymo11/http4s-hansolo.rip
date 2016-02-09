package rip.hansolo.script

import org.scalajs.dom
import rx.async.Timer
import rx.{Ctx, Rx, Var}

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

import ScalatagsRxImplicits._

/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  val secondsPassed = Var(0)
  val helloWorld = Rx("secondsPassed: " + secondsPassed())
  val timer = {
    import Ctx.Owner.Unsafe._
    import rx.async.Platform._
    import scala.concurrent.duration._
    Timer(1 second)
  }

  @JSExport
  override def main(): Unit = {
    println("Hello?")

    timer triggerLater {secondsPassed() = secondsPassed.now + 1}

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      p(helloWorld).render
    )
  }

}
