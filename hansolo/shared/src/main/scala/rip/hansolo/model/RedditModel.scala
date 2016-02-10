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

  case class Description(kind: String, data: Data) extends Data
  object Description {

    def fromValue(str: String): Data = fromValue(read(str))

    def fromValue(value: Js.Value): Data = {
      val kind = value("kind").asInstanceOf[Str].value
      Data.fromValue(value("data"), kind)
    }
  }


  trait Data

  object Data {
    def fromValue(value: Js.Value, kind: String): Data = kind match {
      case "Listing" => Listing.fromValue(value)
      case "t3" => T3.fromValue(value)
      case _ => NoData("No Mapping Found")
    }
  }


  case class Listing(
                      val after: Option[String],
                      before: Option[String],
                      modhash: String, children:
                      Seq[Data]) extends Data

  object Listing {
    def fromValue(value: Js.Value) = Listing(
      value("after"),
      value("before"),
      value("modhash").asInstanceOf[Str].value,
      value("children").asInstanceOf[Arr].value.map(Description.fromValue)
    )
  }


  case class T3(
                 archived: Boolean,
                 author: String,
                 created_utc: Double,
                 domain: String,
                 edited: Boolean,
                 gilded: Int,
                 hide_score: Boolean,
                 id: String,
                 is_self: Boolean,
                 locked: Boolean,
                 name: String,
                 num_comments: Int,
                 over_18: Boolean,
                 permalink: String,
                 score: Int,
                 selftext: String,
                 stickied: Boolean,
                 subreddit: String,
                 thumbnail: String,
                 title: String,
                 url: String
               ) extends Data

  object T3 {
    def fromValue(value: Js.Value) = Try(
      upickle.default.read[T3](write(value))
    ) recover {
      case e: upickle.Invalid.Json => NoData("Invalid Json: " + e.msg)
      case e: upickle.Invalid.Data => NoData("Invalid Data: " + e.msg)
    } getOrElse NoData("Could not read T3")
  }

  case class NoData(message: String) extends Data
}


