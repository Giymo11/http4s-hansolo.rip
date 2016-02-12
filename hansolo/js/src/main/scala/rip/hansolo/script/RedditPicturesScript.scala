package rip.hansolo.script

import rip.hansolo.model.RedditModel._
import rip.hansolo.script.wrapper._
import rip.hansolo.script.util.UriUtils._
import rip.hansolo.script.util.ScalatagsRxImplicits._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.{HashChangeEvent, Event}
import org.scalajs.dom.ext.Ajax

import scala.util.Try
import scalatags.JsDom.all._

import rx._
import rx.async._


/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  import scala.concurrent.ExecutionContext.Implicits.global


  val redditUrl = "https://www.reddit.com"

  @JSExport
  override def main(): Unit = {

    // safe because it is only evaluated once!
    import Ctx.Owner.Unsafe._

    val reddit = Reddit(
      userAgent = "scala-js:ripp.hansolo:v1 (by /u/Giymo11)",
      ImplicitOauth(
        mobile = false,
        clientId = "i7UFQCjP-liV-A",
        redirectUri = "http://localhost/reddit",
        scope = Seq("identity", "read")))



    val queryParams = fromQueryParams(dom.window.location.href)
    dom.window.location.hash = ""

    // access_token, token_type, state, expires_in, scope
    val accessToken = queryParams.get("access_token")
    reddit.attemptAuth(accessToken, queryParams.get("expires_in").map(_.toInt))
    reddit.subredditChanged(queryParams.get("state"))

    reddit.subredditUrl.foreach(subreddit => {
      val newMap = fromQueryParams(dom.window.location.hash) + ("state" -> subreddit)
      dom.window.location.hash = "#" + toQueryParams(newMap)
    })

    // use it to authenticate when the hash changes.
    dom.window.onhashchange = (e: HashChangeEvent) => {
      println("Hash changed: " + e.newURL)
      reddit.subredditChanged(fromQueryParams(e.newURL).get("state"))
    }

    /**
      * The input field to put enter the query
      */
    val queryInput = input(
      id := "query-input",
      placeholder := reddit.subredditUrl,
      autofocus := true
    ).render

    // onchange is fired e.g. when pressing enter or losing focus
    queryInput.onchange = (e: Event) => {
      reddit.subredditChanged(Some(queryInput.value))
      queryInput.value = ""
    }

    val secondsPassed = Var(0)
    val helloWorld = Rx("secondsPassed: " + secondsPassed())
    val timer = {
      import rx.async.Platform._
      import scala.concurrent.duration._
      Timer(1 second)
    }
    timer triggerLater {secondsPassed() = secondsPassed.now + 1}

    /**
      * The String received as Response to the AJAX request.
      * Depends on subredditUrl.
      */
    val responseTextRx = Rx(
      Ajax.get(redditUrl + reddit.subredditUrl())
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
          // the data2frag recursion would be inferred by sbt, but not by intellij.
          listing.children.map(xs => li(data2frag(xs)))
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

      frags.getOrElse(p("fetching response for " + reddit.subredditUrl.now))
    }

    println("Hello?")

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        reddit.authLinkRx,
        queryInput,
        p(reddit.subredditUrl),
        responseFrags
      ).render
    )
  }
}
