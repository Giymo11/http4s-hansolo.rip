package rip.hansolo.script

import rip.hansolo.Config
import rip.hansolo.model.RedditModel._
import rip.hansolo.script.wrapper._
import rip.hansolo.script.util.UriUtils._
import rip.hansolo.script.util.ScalatagsRxImplicits._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.{HashChangeEvent, Event}

import scalatags.JsDom.all._

import rx._
import rx.async._


/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  @JSExport
  override def main(): Unit = {

    // safe because it is only evaluated once!
    import Ctx.Owner.Unsafe._

    val reddit = Reddit(
      userAgent = "scala-js:ripp.hansolo:v1 (by /u/Giymo11)",
      ImplicitOauth(
        mobile = false,
        clientId = Config.clientId,
        redirectUri = "http://localhost/reddit",
        scope = Seq("identity", "read")))

    val queryParams = fromQueryParams(dom.window.location.href)
    dom.window.location.hash = ""

    // access_token, token_type, state, expires_in, scope
    val accessToken = queryParams.get("access_token")
    reddit.attemptAuth(accessToken, queryParams.get("expires_in").map(_.toInt))
    reddit.subredditChanged(queryParams.get("state"))

    reddit.state.foreach(subreddit => {
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
      placeholder := reddit.state,
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
      * The Frags generated dynamically from the RedditModel
      * depends on responseRedditRx.
      */
    val responseFrags: Rx[Frag] = Rx {
      implicit def data2frag(data: Data): Frag = data match {
        case listing: Listing => ul(
          // the data2frag recursion would be inferred by sbt, but not by intellij.
          listing.children.map(xs => li(data2frag(xs)))
        )
        case t3: T3 => div(
            div("Score: ", span(t3.link.score), " - ", span(t3.link.title)),
            img(
              src := t3.link.preview.images.head.source.url,
              maxHeight := 100.pct,
              width := 100.pct
            )
          )
        //case linkpost: Linkpost => span(linkpost.title + " - ", a("link", href := linkpost.url))
        case NoData(msg) => span("NoData: " + msg, backgroundColor := "red")
        case _ => span("Should not happen!", backgroundColor := "red")
      }
      val frags = for {
        description <- reddit.responseRedditRx()
      } yield div(
        description
      )
      frags.getOrElse(p("fetching response for " + reddit.state.now))
    }

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        reddit.authLinkRx,
        div(
          div("Remaining: ", reddit.ratelimitRemaining),
          div("Used: ", reddit.ratelimitUsed),
          div("Reset: ", reddit.ratelimitReset)
        ),
        queryInput,
        p(reddit.state),
        //pre(reddit.responseTextRx),
        responseFrags
      ).render
    )
  }
}
