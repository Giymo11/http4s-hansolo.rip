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
    * The JS Object received as Response to the AJAX request.
    * Depends on responseTextRx.
    */
  val responseJsonRx: Rx[Option[js.Dynamic]] = responseTextRx map (_ map (JSON parse _))

  /**
    * The case class extracted from the AJAX request.
    * depends on responseTextRx
    */
  val responseRedditRx: Rx[Option[Data]] = responseTextRx map (_ map (str => Description.fromValue(str)))

  val responseFrags2: Rx[Frag] = Rx {
    val response = responseRedditRx()


    val frags = for {
      description <- response
    } yield {
      //val kind = description.kind
      //val posts = description.data.asInstanceOf[Listing].children.map(_.data.asInstanceOf[T3])
      div(
        description.toString
      )
    }
  }

  /**
    * The Frags used to render the response.
    * Depends on responseJsonRx.
    */
  val responseFrags: Rx[Frag] = Rx {

    val response = responseJsonRx()

    println(response)

    val frags = for {
      json <- response
    } yield {
      dom.console.dir(json)
      val posts = json.data.children.asInstanceOf[js.Array[js.Dynamic]].toSeq
      val kind = json.kind.asInstanceOf[String]
      val titles = posts.map(_.data.title.asInstanceOf[String])
      div(
        p(s"Kind: $kind, first child: ${titles.head}"),
        ul(
          titles.map(li(_))
        )
      )
    }

    frags.getOrElse(p("fetching response for " + subredditUrl.now))
  }

  val compareResponses = Rx {
    div(
      p(s"Data1: " + responseRedditRx())
      //p(s"Data2: " + responseRedditRx2())
    )
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
        responseFrags,
        compareResponses
      ).render
    )
  }
}
