import sbt._

object libs {

  val `scala-version` = "2.12.6"

  def webjar(name: String, version: String): ModuleID =
    "org.webjars" % name % version

  // https://github.com/milessabin/shapeless
  // Apache 2.0
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  // https://github.com/melrief/pureconfig
  // MPL 2.0
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.9.1"

  // https://github.com/typelevel/cats
  // MIT http://opensource.org/licenses/mit-license.php
  val `cats-core` = "org.typelevel" %% "cats-core" % "1.1.0"

  // https://github.com/functional-streams-for-scala/fs2
  // MIT
  val `fs2-core` = "co.fs2" %% "fs2-core" % "0.10.6-SNAPSHOT"
  val `fs2-io` = "co.fs2" %% "fs2-io" % "0.10.6-SNAPSHOT"
  val `fs2-scodec` = "co.fs2" %% "fs2-scodec" % "0.10.6-SNAPSHOT"

  // https://github.com/Spinoco/fs2-http
  // MIT
  val `fs2-http` = "com.spinoco" %% "fs2-http" % "0.3.0"

  // version that comes with fs2-http has some bugs
  // https://github.com/Spinoco/protocol
  // MIT
  val `spinoco-http` = "com.spinoco" %% "protocol-http" % "0.3.12"
  val `spinoco-mail` = "com.spinoco" %% "protocol-mail" % "0.3.12"

  val `fs2-http-all` = Seq(`fs2-http`, `spinoco-http`, `fs2-scodec`)

  // https://github.com/monix/minitest
  // Apache 2.0
  val minitest = "io.monix" %% "minitest" % "2.1.1"
  val `minitest-laws` = "io.monix" %% "minitest-laws" % "2.1.1"

  // https://github.com/rickynils/scalacheck
  // unmodified 3-clause BSD
  // val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.5"

  // https://github.com/scodec/scodec-bits
  // 3-clause BSD
  val `scodec-bits` = "org.scodec" %% "scodec-bits" % "1.1.5"
  // https://github.com/scodec/scodec
  // 3-clause BSD
  val `scodec-core` = "org.scodec" %% "scodec-core" % "1.10.3"
  val `scodec-stream` = "org.scodec" %% "scodec-stream" % "1.1.0"

  // https://github.com/tpolecat/doobie
  // MIT
  val `doobie-core` = "org.tpolecat" %% "doobie-core" % "0.5.1"
  val `doobie-hikari` = "org.tpolecat" %% "doobie-hikari" % "0.5.1"

  // https://github.com/eikek/bitpeace
  // MIT
  val `bitpeace-core` = "com.github.eikek" %% "bitpeace-core" % "0.2.0"

  // http://www.bouncycastle.org/java.html
  // MIT
  val bcpg = "org.bouncycastle" % "bcpg-jdk15on" % "1.59"

  // https://jdbc.postgresql.org/
  // BSD
  val postgres = "org.postgresql" % "postgresql" % "42.2.2"

  // https://github.com/h2database/h2database
  // MPL 2.0 or EPL 1.0
  val h2 = "com.h2database" % "h2" % "1.4.197"

  // https://github.com/circe/circe
  // ASL 2.0
  val `circe-core` = "io.circe" %% "circe-core" % "0.9.3"
  val `circe-generic` = "io.circe" %% "circe-generic" % "0.9.3"
  val `circe-parser` = "io.circe" %% "circe-parser" % "0.9.3"

  // http://tika.apache.org
  // ASL 2.0
  val tika = "org.apache.tika" % "tika-core" % "1.18"

  // https://github.com/Log4s/log4s
  // ASL 2.0
  val log4s = "org.log4s" %% "log4s" % "1.6.1"

  // http://logback.qos.ch/
  // EPL1.0 or LGPL 2.1
  val `logback-classic` = "ch.qos.logback" % "logback-classic" % "1.2.3"

  // https://github.com/t3hnar/scala-bcrypt
  // ASL 2.0
  // using:
  // - jbcrypt: ISC/BSD
  val `scala-bcrypt` = "com.github.t3hnar" %% "scala-bcrypt" % "3.1"

  // https://github.com/Semantic-Org/Semantic-UI
  // MIT
  val `semantic-ui` = webjar("Semantic-UI", "2.3.3")

  // https://github.com/23/resumable.js
  // MIT
  val resumablejs = webjar("resumable.js", "1.0.2")

  // https://github.com/jquery/jquery
  // MIT
  val jquery = webjar("jquery", "3.3.1")

  // https://highlightjs.org/
  // BSD
  val highlightjs = "org.webjars.bower" % "highlightjs" % "9.12.0"

  // https://java.net/projects/javamail/pages/Home
  // CDDL 1.0, GPL 2.0
  val `javax-mail-api` = "javax.mail" % "javax.mail-api" % "1.6.1"
  val `javax-mail` = "com.sun.mail" % "javax.mail" % "1.6.1"

  // http://dnsjava.org/
  // BSD
  val dnsjava = "dnsjava" % "dnsjava" % "2.1.8" intransitive()

  // https://github.com/eikek/yamusca
  // MIT
  val `yamusca-core` = "com.github.eikek" %% "yamusca-core" % "0.4.0"

  // https://github.com/scopt/scopt
  // MIT
  val scopt = "com.github.scopt" %% "scopt" % "3.7.0"

  // https://github.com/vsch/flexmark-java
  // BSD 2-Clause
  val `flexmark-core` = "com.vladsch.flexmark" % "flexmark" % "0.32.18"
  val `flexmark-gfm-tables` = "com.vladsch.flexmark" % "flexmark-ext-gfm-tables" % "0.32.18"
  val `flexmark-gfm-strikethrough` = "com.vladsch.flexmark" % "flexmark-ext-gfm-strikethrough" % "0.32.18"
  val `flexmark-formatter` = "com.vladsch.flexmark" % "flexmark-formatter" % "0.32.18"

  // https://github.com/jhy/jsoup
  // MIT
  val jsoup = "org.jsoup" % "jsoup" % "1.11.2"
}
