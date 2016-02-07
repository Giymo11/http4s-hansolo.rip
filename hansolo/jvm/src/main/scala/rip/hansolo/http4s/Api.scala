package rip.hansolo.http4s

import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

import org.http4s.MediaType._
import org.http4s.headers.{`Content-Type`, Host}
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent.ResourceService
import org.http4s._
import rip.hansolo.http4s.service._

import scalaz._
import scalaz.concurrent.Task

object Api extends App {

  import org.http4s.dsl._

  /**
    * Checks if the Host String fulfills the predicate
    *
    * @param predicate The function the domain is tested for
    * @return The transformed service returning NotFound if the host does not satisfy the predicate
    */
  def domainFilter(predicate: String => Boolean): HttpMiddleware = Middleware[Request, Response, Request, Response] {
    (req, service) => {
      val uriHost: Option[String] = req.uri.host.map(_.value)
      val headerHost: Option[String] = req.headers.get(Host).map(_.value)
      val hostString: Option[String] = uriHost.orElse(headerHost)

      import scalaz.syntax.std.option._

      //val combinedPredicate = (domain: String) => domain.contains("localhost") || predicate(domain)

      val hostDisjunction = for {
        hostFound <- hostString \/> "no domain found in request"
        hostValid <- (Option(hostFound) filter predicate) \/> "host is not satisfying predicate"
      } yield hostValid

      hostDisjunction match {
        case -\/(message) =>
          println(message)
          NotFound()
        case \/-(host) =>
          println("domain is passing")
          service.run(req)
      }
    }
  }

  /**
    * A filter for domains used for testing
    */
  val testingDomainFilter = domainFilter((domain: String)  => domain.contains("test") || domain.contains("localhost"))
  val tilDomainFilter = domainFilter((domain: String)  => domain.contains("til") || domain.contains("blog"))

  import staticcontent.resourceService
  import ResourceService.Config

  val redirectToKoiService = HttpService { case GET -> Root => SeeOther(Uri.fromString("http://koi.moe").getOrElse(Uri())) }
  val gdocsService = HttpService { case GET -> Root =>
    Ok(<html>
          <body style="margin: 0">
            <iframe style="width: 100%; height: 100%; border: 0; margin: 0"
            src="https://docs.google.com/document/d/1EKExnRpNokVGlOvYpKQA2COOrITBlywvgANAN9qNnEE/pub?embedded=true"></iframe>
          </body>
        </html>.toString())
      .withContentType(Some(`Content-Type`(`text/html`))) }


  // the 0.0.0.0 enables it to be picked up from outside
  BlazeBuilder.bindHttp(80, "0.0.0.0")
    .mountService(tilDomainFilter(gdocsService), "/")
    .mountService(testingDomainFilter(TellMeService()), "/info")
    .mountService(HelloWorldService(), "/hello")
    .mountService(GithubWebhookService(), "/webhook")
    .mountService(GameService(), "/game")
    .mountService(resourceService(Config(basePath = "")), "/public")
    .mountService(redirectToKoiService, "/moe")
    .mountService(MainPageService(), "/")
    .run
    .awaitShutdown()

}
