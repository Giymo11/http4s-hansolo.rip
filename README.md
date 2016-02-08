simple-http4s-api
=================

[![Build Status](https://travis-ci.org/Giymo11/http4s-hansolo.rip.svg)](https://travis-ci.org/Giymo11/http4s-hansolo.rip)

This will be the http4s server running on [hansolo.rip](hansolo.rip).
I used the project template by gvolpe called [simple-http4s-api](https://github.com/gvolpe/simple-http4s-api), which proved to be very helpful.
Lots of inspration taken from lihaoyi's [autowire-example](https://github.com/lihaoyi/workbench-example-app/tree/autowire).

This Project aims to explore the possibilities of scala in the web. 
It uses [http4s](http4s.org) to serve its content, and [scala-js](scala-js.org) instead of JavaScript.

At te moment, it uses libraries such as:
* FP: [scalaz](https://github.com/scalaz/scalaz)
* HTML: [scalatags](https://github.com/lihaoyi/scalatags)
* CSS: [scalacss](https://github.com/japgolly/scalacss)

Some possible additions would be:
* JSON: [argonaut](https://github.com/argonaut-io/argonaut)
* RMI: [autowire](https://github.com/lihaoyi/autowire)
* DB: [doobie](https://github.com/tpolecat/doobie)
* RX: [scala-rx](https://github.com/lihaoyi/scala.rx)


Plugins used:
* [Revolver](https://github.com/spray/sbt-revolver)
* [Update](https://github.com/rtimush/sbt-updates)
Possible Additions:
* some coverage plugin
* [workbench](https://github.com/lihaoyi/workbench)


To run this project, use ```sbt re-start```


My plan is to slowly raise this project to be a good example of an allround web project in Scala, and document the way.