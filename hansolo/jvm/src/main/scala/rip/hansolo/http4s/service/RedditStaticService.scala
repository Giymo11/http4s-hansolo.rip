package rip.hansolo.http4s.service

import org.http4s._
import org.http4s.dsl._
import rip.hansolo.model.RedditModel.Thing
import rip.hansolo.view.RedditRenderer

import scalatags.Text.all._
import scalaz.concurrent.Task

import org.http4s._
import org.http4s.dsl._
import org.http4s.client._
import org.http4s.blaze.http.HttpClient

import scalatags._
import scalatags.Text._
import scalatags.Text.all._

/**
  * Created by Giymo11 on 14.02.2016.
  */
object RedditStaticService {

  def apply(): HttpService = service

  private val service = HttpService.lift { req =>
    val subreddit = "/r/" + req.pathInfo
    println("Wat? " + subreddit)

    // make the request to reddit

    // TODO: abstract into Reddit Wrapper

    val redditUrl = "https://www.reddit.com"

    val requestUri = redditUrl + subreddit + ".json?raw_json=1"

    val client = org.http4s.client.blaze.defaultClient
    val response: Task[String] = client.getAs[String](Uri.fromString(requestUri).getOrElse(uri("https://www.reddit.com/r/reddit_api_test.json?raw_json=1")))

    val responseData = response.flatMap(responseText => Task.now(
      Thing.fromValue(responseText)
    ))

    val frags: Task[Frag] = responseData.flatMap(data => {
      val renderer = RedditRenderer(scalatags.Text)
      Task.now(renderer.data2frag(data))
    })

    frags.flatMap(frag => ScalatagsService(
      "Reddit Static",
      Seq(frag),
      useMdl = true
    ).run(req))
  }
}
