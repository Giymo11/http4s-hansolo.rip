import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import rx._

import scalatags.JsDom.all._

/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  val secondsPassed = Var(0)
  val helloWorld = Rx("secondsPassed: " + secondsPassed())

  implicit def rxToFrag[T](r: Rx[T])(implicit converter: T => Frag): Frag = {
    def rToFrag = span(r.now).render
    var current = rToFrag

    r triggerLater {
      val newCurrent = rToFrag
      js.Dynamic.global.last = current
      current.parentNode.replaceChild(newCurrent, current)
      current = newCurrent
    }

    current
  }

  @JSExport
  override def main(): Unit = {
    println("Hello?")

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      p(helloWorld).render
    )

    dom.setInterval(() => secondsPassed() = secondsPassed.now + 1, 1000)
  }

}
