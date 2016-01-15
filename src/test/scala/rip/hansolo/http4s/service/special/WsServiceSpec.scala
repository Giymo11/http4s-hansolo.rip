package rip.hansolo.http4s.service.special

import com.gvolpe.api.service.HttpServiceSpec
import org.http4s.{Request, Status, Uri}
import rip.hansolo.http4s.service.HttpServiceSpec

class WsServiceSpec extends HttpServiceSpec {

  "WS Service" should {

    val service = WsService()

    "Not find the unknown url request" in {
      val request = new Request(uri = Uri(path = "/unknown"))
      val response = service.run(request).run

      response.status should be (Status.NotFound)
    }

    "Get the welcome message" in {
      val request = new Request()
      val response = service.run(request).run

      response.status should be (Status.NotImplemented)
      response.body should be ("This is a WebSocket route.".asByteVector)
    }

  }

}
