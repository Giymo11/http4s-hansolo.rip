package rip.hansolo.script

import rip.hansolo.model.RedditModel._

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
    * The String received as Response to the AJAX request.
    * Depends on subredditUrl.
    */
  val responseTextRx = Rx(
    Ajax.get(redditUrl + subredditUrl())
      .map[Option[String]](xhr => Some(xhr.responseText))
      .toRx(None)
    ) map (_.apply()) // needed until flatMap works.

  /**
    * The case class extracted from the AJAX request.
    * depends on responseTextRx
    */
  val responseRedditRx: Rx[Option[Data]] = responseTextRx map (_ map (str => Description.fromValue(str)))

  /**
    * The Frags generated dynamically from the RedditModel
    * depends on responseRedditRx.
    */
  val responseFrags: Rx[Frag] = Rx {
    implicit def data2frag(data: Data): Frag = data match {
      case listing: Listing => ul(
        listing.children.map(li(_))
      )
      case selfpost: Selfpost => span(selfpost.title, p(selfpost.selftext))
      case linkpost: Linkpost => span(linkpost.title + " - ", a("link", href := linkpost.url))
      case _ => span("Should not happen!", backgroundColor := "red")
    }

    val frags = for {
      description <- responseRedditRx()
    } yield div(
      description
    )

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
