package rip.hansolo.util

import scala.scalajs.js.URIUtils

/**
  * Created by Giymo11 on 14.02.2016.
  */
object UriCodec {
  def encodeUriComponent(in: String): String = URIUtils.encodeURIComponent(in)
  def decodeUriComponent(in: String): String = URIUtils.decodeURIComponent(in)
}
