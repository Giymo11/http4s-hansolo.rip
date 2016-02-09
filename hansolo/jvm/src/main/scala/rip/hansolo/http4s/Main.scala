package rip.hansolo.http4s

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._
import org.http4s.server.syntax._
import org.http4s.server.staticcontent._
import org.http4s.server.staticcontent.ResourceService._
import org.http4s.server.blaze.BlazeBuilder

import rip.hansolo.http4s.service._
import rip.hansolo.http4s.dsl.HttpServiceOps._

import scalaz._

object Main extends App {

  val staticResourceService = resourceService(Config(basePath = ""))
  val redirectToKoiService = HttpService.lift(req => SeeOther(Uri.fromString("http://koi.moe").getOrElse(Uri())))


  type Path = String
  type MountingMap = Map[Path, NonEmptyList[HttpService]]

  /** the map with all the mounting points. The first service not returning a fallthrough response will be used. */
  val mountingMap: MountingMap = Map(
      "/" -> NonEmptyList(TilService().forDomain("blog"), MainPageService()),
      "/info" -> NonEmptyList(TellMeService().forDomains(Seq("test", "localhost"))),
      "/hello" -> NonEmptyList(HelloWorldService()),
      "/reddit" -> NonEmptyList(ScalaJsService("Reddit Api Trickery", "rip.hansolo.script.RedditPicturesScript"))
    )

  def mapMountpointsOntoBuilder(initialBuilder: ServerBuilder, mountingMap: MountingMap): ServerBuilder = {
    // fold over all entries in the mountingmap, using the initialBuilder as a start vale
    mountingMap.foldLeft(initialBuilder)(
      (builder, entry) => {
        val (path, services) = entry
        // fold the services together, in that if the first returns HttpService.notFound, the second will be tried, and so on
        import scalaz.syntax.foldable1._
        builder.mountService(services.foldLeft1(_ || _), path)
      }
    )
  }

  // the 0.0.0.0 enables it to be picked up from outside
  val serverBuilder = mapMountpointsOntoBuilder(BlazeBuilder.bindHttp(80, "0.0.0.0"), mountingMap)

  serverBuilder
    .mountService(GithubWebhookService(), "/webhook")
    .mountService(GameService(), "/game")
    .mountService(staticResourceService, "/public")
    .mountService(redirectToKoiService, "/moe")
    .run
    .awaitShutdown()
}
