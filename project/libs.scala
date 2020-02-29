import sbt._

object libs {

  val `scala-version` = "2.12.8"

  def webjar(name: String, version: String): ModuleID =
    "org.webjars" % name % version

  // https://github.com/milessabin/shapeless
  // Apache 2.0
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  // https://github.com/melrief/pureconfig
  // MPL 2.0
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.9.2"

  // https://github.com/functional-streams-for-scala/fs2
  // MIT
  val `fs2-core` = "co.fs2" %% "fs2-core" % "2.1.0"
  val `fs2-io` = "co.fs2" %% "fs2-io" % "2.1.0"
  val `fs2-scodec` = "co.fs2" %% "fs2-scodec" % "0.10.7"

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
  val minitest = "io.monix" %% "minitest" % "2.7.0"
  val `minitest-laws` = "io.monix" %% "minitest-laws" % "2.4.0"

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

  // https://github.com/circe/circe
  // ASL 2.0
  val `circe-core` = "io.circe" %% "circe-core" % "0.13.0"
  val `circe-generic` = "io.circe" %% "circe-generic" % "0.13.0"
  val `circe-parser` = "io.circe" %% "circe-parser" % "0.13.0"

  // http://tika.apache.org
  // ASL 2.0
  val tika = "org.apache.tika" % "tika-core" % "1.23"

  // https://github.com/Log4s/log4s
  // ASL 2.0
  val log4s = "org.log4s" %% "log4s" % "1.8.2"

  // http://logback.qos.ch/
  // EPL1.0 or LGPL 2.1
  val `logback-classic` = "ch.qos.logback" % "logback-classic" % "1.2.3"

  // https://github.com/Semantic-Org/Semantic-UI
  // MIT
  val `semantic-ui` = webjar("Semantic-UI", "2.4.1")

  // https://github.com/jquery/jquery
  // MIT
  val jquery = webjar("jquery", "3.4.1")

  // https://github.com/eikek/yamusca
  // MIT
  val `yamusca-core` = "com.github.eikek" %% "yamusca-core" % "0.4.0"
}
