package rip.hansolo.http4s.service

import org.http4s.HttpService
import org.http4s.MediaType._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`

/**
  * Created by Giymo11 on 08.02.2016.
  */
object TilService {

  def apply() = service

  val service = HttpService.lift( req =>
    Ok(<html>
      <body style="margin: 0">
        <iframe style="width: 100%; height: 100%; border: 0; margin: 0"
                src="https://docs.google.com/document/d/1EKExnRpNokVGlOvYpKQA2COOrITBlywvgANAN9qNnEE/pub?embedded=true"></iframe>
      </body>
    </html>.toString()
    ).withContentType(Some(`Content-Type`(`text/html`)))
  )
}
