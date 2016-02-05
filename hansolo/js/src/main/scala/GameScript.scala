import org.scalajs.dom.ext.KeyCode

import scala.scalajs.js._
import scala.scalajs.js.annotation.JSExport

import scala.scalajs.js._
import org.scalajs.dom
import org.scalajs.dom._

import scala.util.Random

/**
  * Created by Giymo11 on 2016-02-04 at 01:32.
  */
object GameScript extends JSApp {

  def sign(i: Int) = if(i < 0) -1 else if(i > 0) 1 else 0


  case class Vec2(x: Int, y: Int) {
    def +(p: Vec2) = Vec2(x + p.x, y + p.y)
    def -(i: Int) = Vec2(x - i, y - i)
    def -(p: Vec2) = Vec2(x - p.x, y - p.y)
    def *(i: Int) = Vec2(x * i, y * i)
    def signs = Vec2(sign(x), sign(y))
    def value = Math.sqrt(x*x + y*y)
  }
  object Vec2 {
    def random(maxX: Int, maxY: Int) = Vec2(Random.nextInt(maxX), Random.nextInt(maxY))
  }

  val canvas = dom.document.getElementById("canvas")
      .asInstanceOf[html.Canvas]

  val context = canvas.getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]

  val middle = Vec2(dom.innerWidth/2, dom.innerHeight/2)

  var player = middle

  var points = Seq.fill(200)(Vec2.random(dom.innerWidth, dom.innerHeight))

  var useAccumulatedVector = false
  var creepMiddle = false

  var intervalSpeed = 10
  var drawInterval = dom.setInterval(() => run(), 13)

  def draw(): Unit = {
    clear()

    context.fillStyle = "white"
    points.foreach(p => context.fillRect(p.x - 8, p.y - 8, 16, 16))

    context.fillStyle = "red"
    context.fillRect(player.x - 10, player.y - 10, 20, 20)
  }

  def run(): Unit = {
    points = points.map(p1 => {
      // the distances from p1 to all other points
      val distances = points.map(p2 => p2 - p1) ++ Seq(player - p1)
      // the relative vectors to points that are at most 80 pixels away
      val closeOnes = distances.filter(_.value < 80)
      // either the accumulated relative vector, or one of the relative vectors
      val delta = if(useAccumulatedVector) closeOnes.fold(Vec2(0, 0))(_ + _) else closeOnes(Random.nextInt(closeOnes.size))
      // the new position of the point
      p1 + delta * -1 + (if(creepMiddle) (middle - p1).signs else Vec2(0, 0))
    })
    draw()
  }

  def changeIntervalBy(i: Int) = {
    intervalSpeed += i
    dom.clearInterval(drawInterval)
    drawInterval = dom.setInterval(() => run(), 6 * intervalSpeed)
  }

  override def main(): Unit = {
    println("Hello World")
    clear()

    dom.onmousemove = (e: MouseEvent) => player = Vec2(e.clientX.toInt, e.clientY.toInt)
    dom.onkeydown = (e: KeyboardEvent) => e.keyCode match {
      case KeyCode.T => useAccumulatedVector = !useAccumulatedVector
      case KeyCode.M => creepMiddle = !creepMiddle
      case KeyCode.Up => changeIntervalBy(-1)
      case KeyCode.Down => changeIntervalBy(1)
      case _ => //ignored
    }

    changeIntervalBy(0)
    // run draw every 13ms
    dom.setInterval(() => draw(), 13)
  }

  def clear(): Unit = {
    canvas.width = dom.innerWidth
    canvas.height = dom.innerHeight

    context.fillStyle = "black"
    context.fillRect(0, 0, canvas.width, canvas.height)
  }
}
