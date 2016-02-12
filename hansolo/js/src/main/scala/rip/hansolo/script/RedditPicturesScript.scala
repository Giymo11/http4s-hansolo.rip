package rip.hansolo.script

import java.rmi.activation.ActivationGroup_Stub
import javax.jws.soap.SOAPBinding.ParameterStyle

import rip.hansolo.model.RedditModel._

import scala.scalajs.js
import scala.scalajs.js.{URIUtils, JSON, JSApp}
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.raw.{HashChangeEvent, Event}
import org.scalajs.dom.ext.Ajax

import scala.util.Try
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

  val userAgent = "scala-js:ripp.hansolo:v1 (by /u/Giymo11)"

  val redditUrl = "https://www.reddit.com"

  val subredditUrl = Var(
    Try(fromQueryParams(dom.window.location.href)("state")).getOrElse("/r/reddit_api_test.json")
  )

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
    val newMap = fromQueryParams(dom.window.location.hash) + ("state" -> queryInput.value)
    dom.window.location.hash = "#" + toQueryParams(newMap)
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

  case class LoginData(val user: String, val passwd: String, val api_type: String = "json")

  def toQueryParams(params: Map[String, String]): String = {
    params.map(entry => entry._1 + "=" + URIUtils.encodeURIComponent(entry._2)).reduce(_ + "&" + _)
  }
  def fromQueryParams(params: String): Map[String, String] = {
    val afterHash = params.dropWhile(c => c != '#' && c != '?').drop(1)
    if(!afterHash.isEmpty)
      afterHash.split("&").map(_.split("=")).map(seq => (seq(0), URIUtils.decodeURIComponent(seq(1)))).toMap
    else
      Map()
  }

  def getImplicitAuthUrl(state: String): String = {
    val params: Map[String, String] = Map(
      "client_id" -> "i7UFQCjP-liV-A",
      "redirect_uri" -> "http://localhost/reddit",
      "scope" -> Seq("identity", "read", "subscribe", "modconfig").mkString(","),
      "state" -> state,
      "response_type" -> "token")

    val uri = "https://www.reddit.com/api/v1/authorize?" + toQueryParams(params)
    println("Uri: " + uri)
    uri
  }

  val authLink = Rx {
    div(a("Click here to authenticate", href := getImplicitAuthUrl(subredditUrl())))
  }

  @JSExport
  override def main(): Unit = {

    println("Hello?")

    timer triggerLater {secondsPassed() = secondsPassed.now + 1}

    dom.window.onhashchange = (e: HashChangeEvent) => {
      println("Hash changed: " + e.newURL)
      val x = fromQueryParams(e.newURL)
      subredditUrl() = x("state")
    }

    val body = dom.document.body
    body.innerHTML = ""
    body.appendChild(
      div(id := "scalatags",
        p(helloWorld),
        authLink,
        queryInput,
        p(subredditUrl),
        responseFrags
      ).render
    )
  }
}
