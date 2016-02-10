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

  import scala.concurrent.ExecutionContext.Implicits.global

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
    * The JSON received as Response to the AJAX request.
    * Depends on subredditUrl.
    */
  val responseRx = Rx (
    Ajax.get(redditUrl + subredditUrl())
    .map[Option[js.Dynamic]](xhr => Some(JSON.parse(xhr.responseText)))
    .toRx(None)
  )

  /**
    * The Frags used to render the response.
    * Depends on responseRx.
    */
  val responseFrags: Rx[Frag] = Rx {

    val response = responseRx().apply()

    println(response)

    val frags = for {
      json <- response
    } yield {
      val posts = json.data.children.asInstanceOf[js.Array[js.Dynamic]].toSeq
      val kind = json.kind.asInstanceOf[String]
      val titles = posts.map(_.data.title.asInstanceOf[String])
      div(
        p(s"Kind: $kind, first child: ${titles.head}"),
        ul(titles.map(li(_)))
      )
    }

    frags.getOrElse(p("fetching response for " + subredditUrl.now))
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
        responseFrags
      ).render
    )
  }
}
