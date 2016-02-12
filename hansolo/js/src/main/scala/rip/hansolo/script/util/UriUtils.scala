package rip.hansolo.script.util

import scala.scalajs.js.URIUtils

/**
  * Created by Giymo11 on 12.02.2016.
  */
object UriUtils {
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
}
