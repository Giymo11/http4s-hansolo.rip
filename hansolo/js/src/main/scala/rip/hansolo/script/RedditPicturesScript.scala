package rip.hansolo.script

import org.scalajs.dom.html.{Element, Input}
import rip.hansolo.Config
import rip.hansolo.util.UriUtils._
import rip.hansolo.script.util.ScalatagsRxImplicits._
import rip.hansolo.view.RedditRenderer
import rip.hansolo.wrapper._

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, HashChangeEvent, Event}

import scala.util.Try
import scalatags.JsDom
import scalatags.JsDom.{TypedTag, all}
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

    reddit.responseRedditRx.foreach(_ => secondsPassed() = 0)

    val redditRenderer = RedditRenderer(scalatags.JsDom)

    /**
      * The Frags generated dynamically from the RedditModel
      * depends on responseRedditRx.
      */
    val responseFrags: Rx[Frag] = reddit.responseRedditRx.map(
      _.map(data =>
        div(cls := "mdl-grid",
          div(cls := "mdl-cell mdl-cell--8-col mdl-cell--2-offset-desktop",
            redditRenderer.data2frag(data)
          )
        )
      ).getOrElse(p("fetching response for " + reddit.state.now))
    )

    val drawerContent: Rx[Frag] = Rx {
      div(
        div(cls := "mdl-layout__header-row",
          backgroundColor := "transparent",
          padding := 0,
          div(cls := "mdl-layout-spacer"),
          reddit.isAuthed() match {
            case true => h6(margin := 0,
              s"Already authenticated!")
            case false => button("Authenticate",
              onclick := { () => dom.window.location.href = reddit.getImplicitAuthUrl },
              cls := "mdl-button mdl-js-button mdl-button--primary mdl-js-ripple-effect")
          },
          div(cls := "mdl-layout-spacer")
        ),
        div(
          div("Remaining: ", reddit.ratelimitRemaining),
          div("Used: ", reddit.ratelimitUsed),
          div("Reset: ", reddit.ratelimitReset)))
    }

    val actionbarTitle: Frag = Rx(reddit.state() + " - " + secondsPassed() + "s")

    val actionbarRightAligned: Frag =
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
            }}
          )
        )
      )

    val actionbarHeaderBackground = "url('http://i.imgur.com/lvuZhX4.jpg') center"

    val body = dom.document.body
    body.innerHTML = ""
    val child = layout(responseFrags, drawerContent, actionbarTitle, actionbarRightAligned, actionbarHeaderBackground)
    val upgrader = js.Dynamic.global.componentUpgrader
      if(!js.isUndefined(upgrader)) upgrader.upgradeElement(child)
    body.appendChild(child)
  }

  def layout(
              responseFrags: Frag,
              drawerContent: Frag,
              actionbarTitle: Frag,
              actionbarRightAligned: Frag,
              actionbarHeaderBackground: String): Element = {

    val mainTag = "main".tag[Element]
    val styleTag = "style".tag[Element]

    div(
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
          background := actionbarHeaderBackground,
          div(cls := "mdl-layout__header-row",
            backgroundColor := "transparent",
            span(cls := "mdl-layout-title",
              actionbarTitle
            ),
            div(cls := "mdl-layout-spacer"),
            actionbarRightAligned
          )
        ),
        div(cls := "mdl-layout__drawer",
          drawerContent),
        mainTag(cls := "mdl-layout__content",
          responseFrags
        )
      )
    ).render
  }
}
