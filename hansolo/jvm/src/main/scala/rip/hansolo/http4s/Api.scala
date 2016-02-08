package rip.hansolo.http4s

import org.http4s._
import org.http4s.server._
import org.http4s.server.syntax._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent.ResourceService
import org.http4s.MediaType._
import org.http4s.headers.{`Content-Type`, Host}

import rip.hansolo.http4s.service._

import scalaz._

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

      val hostDisjunction = for {
        hostFound <- hostString \/> "no domain found in request"
        hostValid <- (Option(hostFound) filter predicate) \/> "host is not satisfying predicate"
      } yield hostValid

      hostDisjunction match {
        case -\/(message) =>
          println(message)
          HttpService.notFound
        // if you use this instead of just NotFound(), it attaches a key to the attributes of the response,
        // enabling it to fall through to another service via Service.withFallthrough
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

  val redirectToKoiService = HttpService.lift(req => SeeOther(Uri.fromString("http://koi.moe").getOrElse(Uri())))
  val gdocsService = HttpService.lift( req =>
    Ok(<html>
          <body style="margin: 0">
            <iframe style="width: 100%; height: 100%; border: 0; margin: 0"
            src="https://docs.google.com/document/d/1EKExnRpNokVGlOvYpKQA2COOrITBlywvgANAN9qNnEE/pub?embedded=true"></iframe>
          </body>
        </html>.toString()
    ).withContentType(Some(`Content-Type`(`text/html`)))
  )

  type Path = String

  // the map with all the mounting points. The first service not returning a fallthrough response will be used.
  val mountingMap: Map[Path, NonEmptyList[HttpService]] = Map(
    "/" -> NonEmptyList(tilDomainFilter(gdocsService), MainPageService()),
    "/info" -> NonEmptyList(testingDomainFilter(TellMeService())),
    "/hello" -> NonEmptyList(HelloWorldService())
  )

  def mapMountpointsOntoBuilder(initialBuilder: ServerBuilder, mountingMap: Map[Path, NonEmptyList[HttpService]]) = {
    // fold over all entries in the mountingmap, using the initialBuilder as a start vale
    mountingMap.foldLeft(initialBuilder)(
      (builder, entry) => {
        val (path, services) = entry
        import scalaz.syntax.foldable1._
        // fold the services together, in that if the first returns HttpService.notFound, the second will be tried, and so on
        builder.mountService(services.foldLeft1(_ || _), path)
      }
    )
  }

  // the 0.0.0.0 enables it to be picked up from outside
  val builder: ServerBuilder = mapMountpointsOntoBuilder(BlazeBuilder.bindHttp(80, "0.0.0.0"), mountingMap)

  builder
    .mountService(GithubWebhookService(), "/webhook")
    .mountService(GameService(), "/game")
    .mountService(resourceService(Config(basePath = "")), "/public")
    .mountService(redirectToKoiService, "/moe")
    .run
    .awaitShutdown()

}
