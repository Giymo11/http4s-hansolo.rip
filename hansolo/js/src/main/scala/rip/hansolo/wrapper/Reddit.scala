package rip.hansolo.wrapper

/**
  * Created by Giymo11 on 12.02.2016.
  */
// TODO: separate JS specific code, for cross platform compatibility
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

import rip.hansolo.model.RedditModel.{Thing, Data}
import rip.hansolo.util.UriUtils._

import rx._
import rx.async._

case class ImplicitOauth(mobile: Boolean, clientId: String, redirectUri: String, scope: Seq[String])

case class Reddit(userAgent: String, oauth: ImplicitOauth)(implicit ctx: Ctx.Owner) {
  def subredditChanged(opt: Option[String]) = opt match {
    case Some(subreddit) if subreddit != subredditUrl.now => subredditUrl() = subreddit
    case _ => // do nothing
  }

  def attemptAuth(tokenOpt: Option[String], expires: Option[Int] = None): Unit = tokenOpt match {
    case Some(token) if !accessToken.now.contains(token) => accessToken() = Some(token)
    case _ => // do nothing
  }

  private val accessToken = Var[Option[String]](None)
  private val subredditUrl = Var[String]("/r/reddit_api_test.json")
  val state: Rx[String] = subredditUrl
  val isAuthed = Rx[Boolean](accessToken().isDefined)

  def getImplicitAuthUrl: String = {
    val currentSubreddit = subredditUrl.now

    val params: Map[String, String] = Map(
      "client_id" -> oauth.clientId,
      "redirect_uri" -> oauth.redirectUri,
      "scope" -> oauth.scope.mkString(","),
      "state" -> currentSubreddit,
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
  private val responseRx = Rx {
    val tokenOpt = accessToken()

    import scala.concurrent.ExecutionContext.Implicits.global

    val (headers: Map[String, String], baseUrl: String) = tokenOpt match {
      case Some(token) => (Map("Authorization" -> s"bearer $token"),  oauthUrl)
      case _ =>           (Map(),                                     redditUrl)
    }

    Ajax.get(baseUrl + subredditUrl() + "?raw_json=1", headers = headers)
      .map[Option[XMLHttpRequest]](Some(_))
      .toRx(None)
  } map (_.apply()) // needed until flatMap works

  private val notAvailable = "not available"
  val ratelimitRemaining = Var[String](notAvailable)
  val ratelimitReset = Var[String](notAvailable)
  val ratelimitUsed = Var[String](notAvailable)

  /**
    * The String received as Response to the AJAX request.
    * Depends on accessToken and subredditUrl.
    */
  private val responseTextRx: Rx[Option[String]] = responseRx.map(_ map (_.responseText))

  /**
    * The map representing the Headers of the last AJAX request.
    * Depends on responseRx.
    */
  private val responseHeadersRx: Rx[Map[String, String]] = responseRx.map(
    opt => opt.map(
      xhr => Map[String, String](
        "x-ratelimit-remaining" -> xhr.getResponseHeader("x-ratelimit-remaining"),
        "x-ratelimit-reset" -> xhr.getResponseHeader("x-ratelimit-reset"),
        "x-ratelimit-used" -> xhr.getResponseHeader("x-ratelimit-used")
      )
    ).getOrElse(Map[String, String]())
  )

  /**
    * Listens for changes of the responsse headers, to update the ratelimit variables.
    */
  responseHeadersRx.foreach(map =>
    if(map.nonEmpty) {
      println(map)
      val newMap = map.mapValues(str => if(str == null || str == "null" || str.isEmpty) notAvailable else str)
      ratelimitRemaining() = newMap.getOrElse("x-ratelimit-remaining", notAvailable)
      ratelimitReset() = newMap.getOrElse("x-ratelimit-reset", notAvailable)
      ratelimitUsed() = newMap.getOrElse("x-ratelimit-used", notAvailable)
    }
  )

  /**
    * The case class extracted from the AJAX request.
    * depends on responseTextRx
    */
  val responseRedditRx: Rx[Option[Data]] = responseTextRx.map(_ map (str => Thing.fromValue(str)))
}
object Reddit {

}
