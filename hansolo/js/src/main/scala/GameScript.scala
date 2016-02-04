import scala.scalajs.js._
import scala.scalajs.js.annotation.JSExport

import scala.scalajs.js._
import org.scalajs.dom
import org.scalajs.dom._

/**
  * Created by Giymo11 on 2016-02-04 at 01:32.
  */
object GameScript extends JSApp {

  case class Point(x: Int, y: Int) {
    def +(p: Point) = Point(x + p.x, y + p.y)
  }

  val canvas =
    dom.document
      .getElementById("canvas")
      .asInstanceOf[html.Canvas]

  val context = canvas.getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]

  var player = Point(dom.innerWidth/2, dom.innerHeight/2)

  def run(): Unit = {
    clear()

    import scala.util.Random
    val delta = Point(Random.nextInt(40) - 20, Random.nextInt(40) - 20)

    player = player + delta

    context.fillStyle = "white"
    context.fillRect(player.x - 10, player.y - 10, 20, 20)
  }

  override def main(): Unit = {
    println("Hello World")
    clear()

    dom.onmousemove = (e: MouseEvent) => player = Point(e.clientX.toInt, e.clientY.toInt)

    dom.setInterval(() => run(), 20)
  }

  def clear(): Unit = {

    canvas.width = dom.innerWidth
    canvas.height = dom.innerHeight

    context.fillStyle = "black"
    context.fillRect(0, 0, canvas.width, canvas.height)
  }
}
