package rip.hansolo.http4s.dsl

import org.http4s._
import org.http4s.headers.Host
import org.http4s.server._

import scalaz.{\/-, -\/}

/**
  * Created by Giymo11 on 08.02.2016.
  */
class HttpServiceOps(val self: HttpService) {
  /**
    * Restricts the service to only work on the allowed domain
    *
    * @param allowedDomain A String that should occur in the Domain
    * @return
    */
  def forDomain(allowedDomain: String): HttpService = forDomains(Seq(allowedDomain))
  def forDomains(allowedDomains: Seq[String]): HttpService = HttpServiceOps.domainFilterMiddleware((actualDomain: String) => {
    allowedDomains.foldLeft(false)((boolean, allowedDomain) => boolean || actualDomain.contains(allowedDomain))
  })(self)
}

object HttpServiceOps {
  implicit def httpServiceToOps(service: HttpService): HttpServiceOps = new HttpServiceOps(service)

  /**
    * Checks if the Host String fulfills the predicate
    *
    * @param predicate The function the domain is tested for
    * @return The transformed service returning NotFound if the host does not satisfy the predicate
    */
  def domainFilterMiddleware(predicate: String => Boolean): HttpMiddleware = Middleware[Request, Response, Request, Response] {
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
}