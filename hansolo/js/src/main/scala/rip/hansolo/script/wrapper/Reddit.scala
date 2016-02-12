package rip.hansolo.script.wrapper

/**
  * Created by Giymo11 on 12.02.2016.
  */

import rip.hansolo.script.util.UriUtils._
import rx._

import scalatags.JsDom.all._

case class ImplicitOauth(mobile: Boolean, clientId: String, redirectUri: String, scope: Seq[String])

case class Reddit(userAgent: String, oauth: ImplicitOauth)(implicit ctx: Ctx.Owner) {
  def subredditChanged(subreddit: Option[String]) = subreddit match {
    case Some(x) if x != subredditUrl.now => subredditUrl() = x
    case _ => // do nothing
  }

  def authLinkRx: Rx[Frag] = Rx {
    div(
      accessToken() match {
        case Some(token) => span(s"Already authenticated! Token: $token")
        case None => a("Click here to authenticate", href := getImplicitAuthUrl(subredditUrl()))
      }
    )
  }

  def attemptAuth(token: Option[String], expires: Option[Int]): Unit = {
    attemptAuth(token)
  }
  def attemptAuth(opt: Option[String]): Unit = opt match {
    case Some(token) if !accessToken.now.contains(token) => accessToken() = Some(token)
    case _ => // do nothing
  }

  val accessToken = Var[Option[String]](None)
  val subredditUrl = Var[String]("/r/reddit_api_test.json")

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
}
object Reddit {

}
