package rip.hansolo.util

import org.http4s.util.string._

/**
  * Created by Giymo11 on 14.02.2016.
  */
object UriCodec {
  def encodeUriComponent(in: String): String = in.formEncode
  def decodeUriComponent(in: String): String = in.formDecode
}
