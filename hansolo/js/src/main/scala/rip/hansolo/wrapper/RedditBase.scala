package rip.hansolo.wrapper

import rx.Ctx

/**
  * Created by Giymo11 on 14.02.2016.
  */


case class ImplicitOauth(mobile: Boolean, clientId: String, redirectUri: String, scope: Seq[String])

class RedditBase(userAgent: String, oauth: ImplicitOauth)(implicit ctx: Ctx.Owner) {

}
