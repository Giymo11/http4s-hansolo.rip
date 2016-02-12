package rip.hansolo.model

import upickle.json._
import upickle.Js
import upickle.Js._

import scala.util.Try



object RedditModel {
    
  implicit def value2option[T](value: Js.Value): Option[T] = value match {
    case Null => None
    case x => x.value match {
      case y: T => Some(y)
      case _ => None
    }
  }

  // cannot be sealded, as upickle then searches for a tagged class.
  trait Data
  object Data {
    def fromValue(value: Js.Value, kind: String): Data = kind match {
      case "Listing" => Listing.fromValue(value)
      case "t3" => T3.fromValue(value)
      case _ => NoData("No Mapping Found")
    }
  }

  case class Thing(kind: String, data: Data) extends Data
  object Thing {

    def fromValue(str: String): Data = fromValue(read(str))

    def fromValue(value: Js.Value): Data = {
      val kind = value("kind").asInstanceOf[Str].value
      Data.fromValue(value("data"), kind)
    }
  }


  case class Listing(
                      val after: Option[String],
                      val before: Option[String],
                      val children: Seq[Data]) extends Data
  object Listing {
    def fromValue(value: Js.Value) = Listing(
      value("after"),
      value("before"),
      value("children").asInstanceOf[Arr].value.map(Thing.fromValue)
    )
  }

  case class Votable(ups: Int, downs: Int, likes: Boolean)
  case class Created(created: Double, created_utc: Double)

  case class Link(
                 title: String,
                 score: Int,

                 author: String,
                 // author_flair_css_class: String,
                 author_flair_text: String,
                 // clicked: Boolean,
                 domain: String,
                 hidden: Boolean,
                 is_self: Boolean,
                 // link_flair_css: Boolean,
                 link_flair_text: String,
                 locked: Boolean,
                 // media
                 // media_embed
                 num_comments: Int,
                 over_18: Boolean,
                 permalink: String,
                 saved: Boolean,

                 // selftext: String,
                 selftext_html: String,
                 subreddit: String,
                 // subreddit_id: String,
                 thumbnail: String,

                 url: String,
                 //edited: Double, this is either Boolean or Long...
                 distinguished: String,
                 stickied: Boolean
                 )
  case class Preview(images: Seq[Image])
  case class Image(source: Resolution)
  case class Resolution(
                       height: Int,
                       width: Int,
                       url: String
                       )
  case class Media(oembed: OEmbed, `type`: String)
  case class OEmbed(
                    //author_name: String,    youtube only
                    //author_url: String,       youtube only
                    description: String,
                    html: String,
                    provider_name: String,
                    provider_url: String,
                    thumbnail_url: String,
                    title: String,
                    `type`: String,
                    //  url: String,      youtub only
                    version: String
                    )

  case class T3(
                votable: Votable,
                created: Created,
                link: Link,
                preview: Option[Preview],
                media: Option[Media]
               ) extends Data
  object T3 {
    def fromValue(value: Js.Value) = Try {
      val votable = upickle.default.read[Votable](write(value))
      val created = upickle.default.read[Created](write(value))
      val link = upickle.default.read[Link](write(value))
      val pairs = value.asInstanceOf[Obj].value
      val preview = pairs.find(_._1 == "preview").map(_._2).map(opt => upickle.default.read[Preview](write(opt))).flatMap(Option(_)) // flatmap because you can still get an Option[Media] with Null
      val media = pairs.find(_._1 == "media").map(_._2).map(opt => upickle.default.read[Media](write(opt))).flatMap(Option(_))

      T3(votable, created, link, preview, media)
    } recover {
      case e: upickle.Invalid.Json => NoData("Invalid Json: " + e.msg)
      case e: upickle.Invalid.Data => NoData("Invalid Data: " + e.msg + e.data)
    } getOrElse NoData("Could not read T3")
  }

  case class NoData(message: String) extends Data
}


