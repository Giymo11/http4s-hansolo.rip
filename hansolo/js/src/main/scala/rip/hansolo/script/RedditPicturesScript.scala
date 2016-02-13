package rip.hansolo.script

import org.scalajs.dom.html.Input
import rip.hansolo.Config
import rip.hansolo.model.RedditModel._
import rip.hansolo.script.wrapper._
import rip.hansolo.script.util.UriUtils._
import rip.hansolo.script.util.ScalatagsRxImplicits._

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.{HashChangeEvent, Event}

import scala.util.Try
import scalatags.JsDom.all._

import rx._
import rx.async._

import util.ScalatagsRxImplicits.MdlConverter._


/**
  * Created by Giymo11 on 09.02.2016.
  */
object RedditPicturesScript extends JSApp {

  @JSExport
  override def main(): Unit = {

    // safe because it is only evaluated once!
    import Ctx.Owner.Unsafe._

    val reddit = Reddit(
      userAgent = "scala-js:rip.hansolo:v1 (by /u/Giymo11)",
      ImplicitOauth(
        mobile = false,
        clientId = Config.clientId,
        redirectUri = Config.redirectUrl,
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
        case t3: T3 => Try(
          div(
            div("Score: ", span(t3.link.score), " - ", span(t3.link.title)),
            if(t3.link.is_self) {
              p(raw(t3.link.selftext_html))
            } else {
              if(t3.media.isDefined)
                div(raw(t3.media.get.oembed.html))
              else if(t3.preview.isDefined)
                img(
                  src := t3.preview.get.images.head.source.url,
                  maxHeight := "80vh",
                  maxWidth := 100.pct)
              else
                div(a(href := t3.link.url))}
          )
        ).recover {
          case e: Exception => div(span("Exception: " + e))
        }.get
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

    reddit.responseRedditRx.foreach(_ => secondsPassed() = 0)

    val authLinkRx: Rx[Frag] = Rx { div(
      reddit.isAuthed() match {
        case true => span(s"Already authenticated!")
        case false => div(
          button("Authenticate",
            onclick := { () => dom.window.location.href = reddit.getImplicitAuthUrl},
            cls := "mdl-button mdl-js-button mdl-button--primary mdl-js-ripple-effect")
        )
      }
    )}

    val mainTag = "main".tag[dom.html.Element]
    val styleTag = "style".tag[dom.html.Element]

    val body = dom.document.body
    body.innerHTML = ""
    val child = div(
      styleTag("type".attr := "text/css",
        raw(
          """
            |.mdl-layout__tab-bar-button {
            |  background-color: transparent;
            |}
            |.mdl-layout__drawer-button {
            |  display: flex;
            |  justify-content: center;
            |  align-items: center;
            |}
          """.stripMargin),
        "scoped".attr := true),
      div(cls := "mdl-layout mdl-js-layout mdl-layout--fixed-header",
        header(cls := "mdl-layout__header",
          background := "url('http://i.imgur.com/lvuZhX4.jpg') center",
          div(cls := "mdl-layout__header-row",
            backgroundColor := "transparent",
            span(cls := "mdl-layout-title", Rx(reddit.state() + " - " + secondsPassed() + "s")),
            div(cls := "mdl-layout-spacer"),
            div(cls := "mdl-textfield mdl-js-textfield mdl-textfield--expandable mdl-textfield--floating-label mdl-textfield--align-right",
              label(cls := "mdl-button mdl-js-button mdl-button--icon",
                `for` := "fixed-header-drawer-exp",
                i(cls := "material-icons",
                  "search")),
              div(cls := "mdl-textfield__expandable-holder",
                input( // The input field to enter the query
                  cls := "mdl-textfield__input",
                  `type` := "text",
                  name := "sample",
                  id := "fixed-header-drawer-exp",
                  placeholder := reddit.state,
                  autofocus := true,
                  onchange := { (e: Event) => { // onchange is fired e.g. when pressing enter or losing focus
                  val target = e.target.asInstanceOf[Input]
                    reddit.subredditChanged(Some(target.value))
                    target.value = ""
                  }})))),
          div(cls := "mdl-layout__tab-bar mdl-js-ripple-effect",
            backgroundColor := "transparent",
            Seq("Hot", "Top", "Controversial").map(tabTitle => a(tabTitle,
              href := s"#scroll-tab-$tabTitle", cls := "mdl-layout__tab" + (if(tabTitle == "Hot") " is-active" else "")))
          )
        ),
        div(cls := "mdl-layout__drawer",
          div(
            authLinkRx,
            div(
              div("Remaining: ", reddit.ratelimitRemaining),
              div("Used: ", reddit.ratelimitUsed),
              div("Reset: ", reddit.ratelimitReset)))),
        mainTag(cls := "mdl-layout__content",
          div(id := "scalatags",
            p(reddit.state),
            responseFrags
          )
        )
      )
    ).render
    val upgrader = js.Dynamic.global.componentUpgrader
      if(!js.isUndefined(upgrader)) upgrader.upgradeElement(child)
    body.appendChild(child)
  }
}
