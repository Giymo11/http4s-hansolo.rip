package rip.hansolo.script

import scala.concurrent.{ExecutionContext, Future}

import scala.scalajs.js
import scala.scalajs.js.{JSON, JSApp}
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax

import scalatags.JsDom.all._

import rx._
import rx.async._

import ScalatagsRxImplicits._

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

  val redditUrl = "https://www.reddit.com"
  val subredditUrl = Var("/r/reddit_api_test.json")

  /**
    * The input field to put enter the query
    */
  val queryInput = input(
    id := "query-input",
    placeholder := subredditUrl,
    autofocus := true
  ).render

  // onchange is fired e.g. when pressing enter or losing focus
  queryInput.onchange = (e: Event) => {
    subredditUrl() = queryInput.value
    queryInput.value = ""
  }

  /**
    * The Frags sent as Response to the JSON request.
    * Depends on subredditUrl.
    */
  val responseRx = {
    import scala.concurrent.ExecutionContext.Implicits.global

    /**
      * The future of finished Frags to put into the dom.
      */
    val futureRx = Rx[Future[Frag]] {
      Ajax.get(redditUrl + subredditUrl())
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
    }

    // this should work, but is currently bugged. To be fixed in scala.rx 0.3.1
    //futureRx.flatMap(future => futureToRx(future, p("fetching response for " + subredditUrl())))
    futureRx.map(future => future.toRx(p("fetching response for " + subredditUrl.now)))
  }

  @JSExport
  override def main(): Unit = {
    println("Hello?")

    timer triggerLater {secondsPassed() = secondsPassed.now + 1}

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        queryInput,
        p(subredditUrl),
        responseRx
      ).render
    )
  }
}
