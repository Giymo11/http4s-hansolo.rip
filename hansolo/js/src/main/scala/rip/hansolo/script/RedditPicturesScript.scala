package rip.hansolo.script

import org.scalajs.dom
import rx.async.Timer
import rx.{Ctx, Rx, Var}

import scala.scalajs.js
import scala.scalajs.js.{JSON, JSApp}
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

import ScalatagsRxImplicits._

import dom.ext.Ajax

import js.JSConverters._

/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  val secondsPassed = Var(0)
  val helloWorld = Rx("secondsPassed: " + secondsPassed())
  val timer = {
    import rx.async.Platform._
    import scala.concurrent.duration._
    Timer(1 second)
  }

  val redditResponse: Var[Frag] = Var(p("No Response Yet"))

  @JSExport
  override def main(): Unit = {
    println("Hello?")

    timer triggerLater {secondsPassed() = secondsPassed.now + 1}

    val testSubredditUrl = "https://www.reddit.com/r/reddit_api_test.json"

    val startAjax = () => {
      import scala.concurrent.ExecutionContext.Implicits.global
      val future = Ajax.get(testSubredditUrl)
      future.onSuccess { case xhr =>
        val json = JSON.parse(xhr.responseText)

        val posts: Seq[js.Dynamic] = json.data.children.asInstanceOf[js.Array[js.Dynamic]]
        val titles = posts.map(child => child.data.title.asInstanceOf[String])

        println("worked?")
        redditResponse() = p("Kind: " + json.kind + ", first child: " + titles.head)
      }
      future.onFailure { case e => redditResponse() = "Error: " + e.getMessage}
    }

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        button("start ajax", onclick := startAjax),
        redditResponse
      ).render
    )

  }
}
