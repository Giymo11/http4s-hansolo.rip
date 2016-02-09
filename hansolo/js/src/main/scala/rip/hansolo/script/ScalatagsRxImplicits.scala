package rip.hansolo.script

import rx.Rx

import scala.scalajs.js
import scalatags.JsDom.all._

/**
  * Created by Giymo11 on 09.02.2016.
  */
object ScalatagsRxImplicits {

  /**
    * Converts a Rx to a Scalatag and keeps it up to date.
    *
    * @param r The Rx to convert
    * @param converter
    * @tparam T The result-type of the Rx
    * @return The Frag to be used with a Scalatag
    */
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
}
