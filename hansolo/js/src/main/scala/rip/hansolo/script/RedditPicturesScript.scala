package rip.hansolo.script

import org.scalajs.dom
import org.scalajs.dom.raw.Event
import rx.async.Timer
import rx.{Ctx, Rx, Var}

import scala.concurrent.Future
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

    val redditUrl = "https://www.reddit.com"
    val subredditUrl = Var("/r/reddit_api_test.json")

    val queryInput = input(
      id := "query-input",
      placeholder := subredditUrl,
      autofocus := true
    ).render

    queryInput.onchange = (e: Event) => {
      subredditUrl() = queryInput.value
      queryInput.value = ""
    }

    val startAjax = () => {
      import scala.concurrent.ExecutionContext.Implicits.global

      val testSubredditUrl = redditUrl + subredditUrl.now

      val future = Ajax.get(testSubredditUrl)
        .map(xhr => JSON.parse(xhr.responseText))
        .map(json => {
          val posts: Seq[js.Dynamic] = json.data.children.asInstanceOf[js.Array[js.Dynamic]]
          (json.kind.asInstanceOf[String], posts.map(_.data.title.asInstanceOf[String]))
        }).map{ case(kind, titles) =>
          div(
            p("Kind: " + kind + ", first child: " + titles.head),
            ul(titles.map(li(_)))
          )
        }.recover { case e =>
          span(e.getMessage, backgroundColor := "red")
        }
      future foreach (ele => redditResponse() = ele)
    }

    subredditUrl foreach (newValue => startAjax())

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        queryInput,
        p(subredditUrl),
        button("start ajax", onclick := startAjax),
        redditResponse
      ).render
    )
  }
}
