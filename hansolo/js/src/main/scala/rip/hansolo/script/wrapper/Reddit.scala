package rip.hansolo.script.wrapper

/**
  * Created by Giymo11 on 12.02.2016.
  */

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import rip.hansolo.model.RedditModel.{Thing, Data}
import rip.hansolo.script.util.UriUtils._
import rx._
import rx.async._

import scalatags.JsDom.all._

case class ImplicitOauth(mobile: Boolean, clientId: String, redirectUri: String, scope: Seq[String])

case class Reddit(userAgent: String, oauth: ImplicitOauth)(implicit ctx: Ctx.Owner) {
  def subredditChanged(opt: Option[String]) = opt match {
    case Some(subreddit) if subreddit != subredditUrl.now => subredditUrl() = subreddit
    case _ => // do nothing
  }

  def authLinkRx: Rx[Frag] = Rx { div(
      accessToken() match {
        case Some(token) => span(s"Already authenticated! Token: $token")
        case None => a("Click here to authenticate", href := getImplicitAuthUrl(subredditUrl()))
      }
  )}

  def attemptAuth(token: Option[String], expires: Option[Int]): Unit = attemptAuth(token)

  def attemptAuth(opt: Option[String]): Unit = opt match {
    case Some(token) if !accessToken.now.contains(token) => accessToken() = Some(token)
    case _ => // do nothing
  }

  private val accessToken = Var[Option[String]](None)
  private val subredditUrl = Var[String]("/r/reddit_api_test.json")
  val state: Rx[String] = subredditUrl

  def getImplicitAuthUrl(state: String): String = {
    val params: Map[String, String] = Map(
      "client_id" -> oauth.clientId,
      "redirect_uri" -> oauth.redirectUri,
      "scope" -> oauth.scope.mkString(","),
      "state" -> state,
      "response_type" -> "token") // for implicit oauth, which is the only supported

    val uri = "https://www.reddit.com/api/v1/authorize?" + toQueryParams(params)
    println("Uri: " + uri)
    uri
  }

  val redditUrl = "https://www.reddit.com"
  // to be used without bearer token
  val oauthUrl = "https://oauth.reddit.com" // to be used with bearer token

  /**
    * The String received as Response to the AJAX request.
    * Depends on accessToken and subredditUrl.
    */
  val responseRx = Rx {
    val tokenOpt = accessToken()

    import scala.concurrent.ExecutionContext.Implicits.global

    val (headers: Map[String, String], baseUrl: String) = tokenOpt match {
      case Some(token) => (Map("Authorization" -> s"bearer $token"),  oauthUrl)
      case _ =>           (Map(),                                     redditUrl)
    }

    Ajax.get(baseUrl + subredditUrl(), headers = headers)
      .map[Option[XMLHttpRequest]](Some(_))
      .toRx(None)
  } map (_.apply()) // needed until flatMap works

  val ratelimitRemaining = Var[String]("600")
  val ratelimitReset = Var[String]("400")
  val ratelimitUsed = Var[String]("0")

  val x = responseRx.foreach(_ foreach (xhr => {
    ratelimitRemaining() = xhr.getResponseHeader("x-ratelimit-remaining")
    ratelimitReset() = xhr.getResponseHeader("x-ratelimit-reset")
    ratelimitUsed() = xhr.getResponseHeader("x-ratelimit-used")
  }))

  /**
    * The String received as Response to the AJAX request.
    * Depends on accessToken and subredditUrl.
    */
  val responseTextRx: Rx[Option[String]] = responseRx.map(_ map (_.responseText))

  /**
    * The case class extracted from the AJAX request.
    * depends on responseTextRx
    */
  val responseRedditRx: Rx[Option[Data]] = responseTextRx.map(_ map (str => Thing.fromValue(str)))
}
object Reddit {

}
