package rip.hansolo.view

import rip.hansolo.model.RedditModel.{Data, Listing, NoData, T3}

import scala.util.Try

/**
  * Created by Giymo11 on 13.02.2016.
  */
case class RedditRenderer[Builder, Output <: FragT, FragT](val bundle: scalatags.generic.Bundle[Builder, Output, FragT]) {
  import bundle.all._

  def renderLinkpost(t3: T3) = {
    if(t3.media.isDefined)
      raw(t3.media.get.oembed.html)
    else if(t3.preview.isDefined)
      div( // TODO: expand on click
        overflow := "hidden",
        maxHeight := "75vh",
        display := "flex",
        alignItems := "center",
        img(
          width := 100.pct,
          height := "auto",
          flex := "none",
          src := t3.preview.get.images.head.source.url
        )
      )
    else
      div(a(href := t3.link.url))
  }

  def renderT3(t3: T3): Frag = {
    Try(
      div(  // TODO: think about super-widescreen usage
        h4("Score: ", span(t3.link.score), " - ", span(t3.link.title)),
        //div("Score: ", span(t3.link.score), " - ", span(t3.link.title)),
        if(t3.link.is_self) raw(t3.link.selftext_html)
        else renderLinkpost(t3)
      )
    ).recover {
      case e: Exception => div(span("Exception: " + e))
    }.get
  }

  implicit def data2frag(data: Data): Frag = data match {
    case listing: Listing => div(
      // the data2frag recursion would be inferred by sbt, but not by intellij.
      listing.children.map(xs => data2frag(xs))
    )
    case t3: T3 => renderT3(t3)
    //case linkpost: Linkpost => span(linkpost.title + " - ", a("link", href := linkpost.url))
    case NoData(msg) => span("NoData: " + msg, backgroundColor := "red")
    case _ => span("Should not happen!", backgroundColor := "red")
  }
}
