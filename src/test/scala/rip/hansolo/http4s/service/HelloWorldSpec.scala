package rip.hansolo.http4s.service

import org.http4s.{Request, Status, Uri}

class HelloWorldSpec extends HttpServiceSpec {

  "Home Service" should {

    val service = HelloWorldService()

    "Not find the unknown url request" in {
      val request = new Request(uri = Uri(path = "/unknown"))
      val response = service.run(request).run

      response.status should be (Status.NotFound)
    }

    "Get the welcome message" in {
      val request = new Request()
      val response = service.run(request).run

      response.status should be (Status.Ok)
      response.body should be ("Hello World!".asByteVector)
    }

  }

}
