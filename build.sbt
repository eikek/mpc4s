import libs._
import Path.relativeTo
import com.typesafe.sbt.SbtGit.GitKeys._

lazy val sharedSettings = Seq(
  scalaVersion := `scala-version`,
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-Xfatal-warnings", // fail when there are warnings
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:higherKinds",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import"
  ),
  scalacOptions in (Compile, console) := Seq(),
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  organization := "com.github.eikek",
  licenses := Seq("GPLv3" -> url("https://spdx.org/licenses/GPL-3.0-or-later.html")),
  homepage := Some(url("https://github.com/eikek/mpc4s")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/eikek/mpc4s.git"),
      "scm:git:git@github.com:eikek/mpc4s.git"
    )
  )
)

lazy val runSettings = Seq(
  fork in run := true,
  javaOptions in run ++= Seq(
    "-Dmpc4s.http.console=true",
    "-Dmpc4s.http.optionalConfig=" + ((baseDirectory in LocalRootProject).value / "dev.conf"),
    "-Xmx96M"
  ),
  javaOptions in reStart := (javaOptions in run).value  ++ Seq("-Dmpc4s.http.console=false"),
  connectInput in run := true
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  developers := List(
    Developer(
      id = "eikek",
      name = "Eike Kettner",
      url = url("https://github.com/eikek"),
      email = ""
    )
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false
)

lazy val testDeps = Seq(minitest, `logback-classic`).map(_ % "test")

lazy val protocol = project.in(file("modules/protocol")).
  enablePlugins(BuildInfoPlugin).
  settings(sharedSettings).
  settings(publishSettings).
  settings(
    name := "mpc4s-protocol",
    description := "The MPD (Music Player Daemon) protocol",
    libraryDependencies ++= Seq(shapeless) ++ testDeps,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, gitHeadCommit, gitHeadCommitDate, gitUncommittedChanges, gitDescribedVersion),
    buildInfoPackage := "mpc4s.protocol",
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime
  )

lazy val client = project.in(file("modules/client")).
  settings(sharedSettings).
  settings(publishSettings).
  settings(
    name := "mpc4s-client",
    description := "A mpd client library based on fs2",
    libraryDependencies ++= Seq(`fs2-core`, `fs2-io`, log4s) ++ testDeps
  ).
  dependsOn(protocol)


def debianSettings(prj: Reference) = Seq(
  maintainer := "Eike Kettner <eike.kettner@posteo.de>",
  packageSummary := description.value,
  packageDescription := description.value,
  mappings in Universal += {
    val conf = (resourceDirectory in (prj, Compile)).value / "reference.conf"
    if (!conf.exists) {
      sys.error(s"File $conf not found")
    }
    conf -> "conf/mpc4s.conf"
  },
  bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/mpc4s.conf""""
)

lazy val http = project.in(file("modules/http")).
  enablePlugins(JavaServerAppPackaging
    , DebianPlugin
    , SystemdPlugin).
  settings(sharedSettings).
  settings(publishSettings).
  settings(runSettings).
  settings(debianSettings(project)).
  settings(
    name := "mpc4s-http",
    description := "A http interface to the music player daemon",
    libraryDependencies ++= testDeps ++ Seq(`fs2-http`, pureconfig, `logback-classic`, `circe-core`, `circe-parser`, `circe-generic`, tika)
  ).
  dependsOn(client)

lazy val player = project.in(file("modules/player")).
  enablePlugins(ElmPlugin
    , WebjarPlugin
    , JavaServerAppPackaging
    , DebianPlugin
    , SystemdPlugin).
  settings(sharedSettings).
  settings(runSettings).
  settings(debianSettings(http)).
  settings(
    name := "mpc4s-player",
    description := "A web-based MPD client",
    libraryDependencies ++= Seq(
      `semantic-ui`, jquery
    ),
    elmVersion := "0.18.0 <= v < 0.19.0",
    elmDependencies in Compile ++= Seq(
      "elm-lang/core" -> "5.0.0 <= v < 6.0.0",
      "elm-lang/html" -> "2.0.0 <= v < 3.0.0",
      "elm-lang/http" -> "1.0.0 <= v < 2.0.0",
      "elm-lang/animation-frame" -> "1.0.0 <= v < 2.0.0",
      "elm-lang/navigation" -> "2.0.0 <= v < 3.0.0",
      "elm-lang/websocket" -> "1.0.0 <= v < 2.0.0",
      "elm-community/random-extra" -> "2.0.0 <= v < 3.0.0",
      "evancz/url-parser" -> "2.0.0 <= v < 3.0.0",
      "evancz/elm-markdown" -> "3.0.0 <= v < 4.0.0",
      "truqu/elm-base64" -> "2.0.0 <= v < 3.0.0",
      "elm-tools/parser" -> "2.0.0 <= v < 3.0.0",
      "NoRedInk/elm-decode-pipeline" -> "3.0.0 <= v < 4.0.0"
    ),
    resourceGenerators in Compile += (elmMake in Compile).taskValue,
    // webjar stuff
    resourceGenerators in Compile += (elmMake in Compile).taskValue,
    webjarPackage in (Compile, webjarSource) := "mpc4s.player.webjar",
    sourceGenerators in Compile += (webjarSource in Compile).taskValue,
    resourceGenerators in Compile += (webjarContents in Compile).taskValue,
    resourceGenerators in Compile += (webjarWebPackageResources in Compile).taskValue,
    webjarWebPackages in Compile += Def.task({
      val elmFiles = (elmMake in Compile).value pair relativeTo((elmMakeOutputPath in Compile).value)
      val src = (sourceDirectory in Compile).value
      val htmlFiles = (src/"html" ** "*").get.filter(_.isFile).toSeq pair relativeTo(src/"html")
      val cssFiles = IO.listFiles(src/"css").toSeq pair relativeTo(src/"css")
      val jsFiles = IO.listFiles(src/"js").toSeq pair relativeTo(src/"js")
      WebPackage("org.webjars", name.value, version.value, elmFiles ++ htmlFiles ++ cssFiles ++ jsFiles)
    }).taskValue
  ).dependsOn(http)

lazy val benchmark = project.in(file("modules/benchmark")).
  enablePlugins(JmhPlugin).
  settings(sharedSettings).
  settings(
    name := "mpc4s-benchmark",
    publishArtifact := false,
    libraryDependencies ++= Seq(
    )
  ).
  dependsOn(protocol, client)

lazy val microsite = project.in(file("microsite")).
  enablePlugins(MicrositesPlugin).
  settings(sharedSettings).
  settings(
    name := "mpc4s-microsite",
    publishArtifact := false,
    scalacOptions -= "-Yno-imports",
    scalacOptions ~= { _ filterNot (_ startsWith "-Ywarn") },
    scalacOptions ~= { _ filterNot (_ startsWith "-Xlint") },
    skip in publish := true,
    micrositeFooterText := Some(
      """
        |<p>&copy; 2018 <a href="https://github.com/eikek/mpc4s">mpc4s, v{{site.version}}</a></p>
        |""".stripMargin
    ),
    micrositeName := "mpc4s",
    micrositeDescription := "Scala- and Web-Client for MPD",
    micrositeBaseUrl := "/mpc4s",
    micrositeAuthor := "eikek",
    micrositeGithubOwner := "eikek",
    micrositeGithubRepo := "mpc4s",
    micrositeGitterChannel := false,
    micrositeFavicons := Seq(microsites.MicrositeFavicon("favicon.png", "96x96")),
    micrositeShareOnSocial := false,
    fork in tut := true,
    scalacOptions in Tut ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
    resourceGenerators in Tut += Def.task {
      val conf = (resourceDirectory in (http, Compile)).value / "reference.conf"
      val out = resourceManaged.value/"main"/"jekyll"/"http"/"_reference.conf"
      streams.value.log.info(s"Copying reference.conf: $conf -> $out")
      IO.copy(Seq(conf -> out))
      Seq(out)
    }.taskValue
  ).dependsOn(protocol, client, http)

lazy val root = project.in(file(".")).
  settings(sharedSettings).
  settings(
    name := "root"
  ).
  aggregate(protocol, benchmark, client, http, player, microsite)
