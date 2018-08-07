package mpc4s.http

import java.nio.file.{Path, Paths}
import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicLong
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext

import cats.effect.IO
import cats.implicits._
import fs2.{async, Scheduler, Stream}
import spinoco.fs2.http.routing.Route
import org.log4s._

import mpc4s.protocol.BuildInfo
import mpc4s.protocol.codec.CommandCodec
import mpc4s.http.config.{AppConfig, ServerBind}
import mpc4s.http.util.Files._

final class Main(playerRoute: AppConfig => Option[Route[IO]]) {
  import Main._

  private[this] val logger = getLogger

  implicit val ecGlobal = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool(new ThreadFactory() {
    private val counter = new AtomicLong(0)
    def newThread(r: Runnable) =
      new Thread(r, s"mpc4s-http-${counter.getAndIncrement}")
  }))

  implicit val ACG = AsynchronousChannelGroup.withThreadPool(ecGlobal) // http.server requires a group

  implicit val SCH = Scheduler.fromScheduledExecutorService(Executors.newScheduledThreadPool(10, new ThreadFactory() {
    private val counter = new AtomicLong(0)
    def newThread(r: Runnable) =
      new Thread(r, s"mpc4s-http-${counter.getAndIncrement}")
  }))

  def run(args: Array[String]): Unit = {
    logger.info(s"""
       |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
       | mpc4s-http ${Version.longVersion} build at ${BuildInfo.builtAtString.dropRight(4)}Z is starting up …
       |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––""".stripMargin)

    val params = StartConfig.parse(args.toSeq)
    params.setup.unsafeRunSync

    val appConfig = AppConfig.default
    val appStart: Stream[IO, Unit] =
      Stream.bracket(App.create[IO](appConfig, CommandCodec.defaultConfig, playerRoute(appConfig)))(
        app => {
          val server = new Server(app, ServerBind.default)
          logger.info(s"""
           |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
           | • Starting mpc4s-http server at ${server.bind.host}:${server.bind.port}
           | • Config: ${params.effectiveConfig.getOrElse("default")}
           | • ${appConfig.mpd}
           |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––""".stripMargin)

          Stream.eval(app.start) >>
          (if (params.console) Stream.eval(startWithConsole(server.create))
          else server.create)

        },
        app => app.stop.flatMap(_ => IO(ecGlobal.shutdown())))

    appStart.compile.drain.unsafeRunSync
  }

  private def startWithConsole(server: Stream[IO,Unit]): IO[Unit] = {
    async.signalOf[IO, Boolean](false).flatMap ({ interrupt =>
      for {
        wait1 <- async.start(server.interruptWhen(interrupt).compile.drain)
        _ <- IO(println("Hit RETURN to stop the server"))
        _ <- IO(scala.io.StdIn.readLine())
        _ <- interrupt.set(true)
        _ <- wait1
        _ <- IO(logger.info("mpc4s-http has stopped"))
      } yield ()
    })
  }
}

object Main {

  def main(args: Array[String]): Unit = {
    new Main(_ => None).run(args)
  }


  case class StartConfig(console: Boolean, configFile: Option[Path]) {
    private[this] val logger = getLogger

    def effectiveConfig: Option[Path] =
      (configFile.toList ::: optionalConfig.toList).find(_.exists)

    def setup: IO[Unit] = IO {
      effectiveConfig.foreach { f =>
        logger.info(s"Using config file '$f'")
        System.setProperty("config.file", f.toString)
      }
    }

    def optionalConfig: Option[Path] =
      Option(System.getProperty("mpc4s.http.optionalConfig")).
        flatMap(name => Option(Paths.get(name)).filter(_.exists))
  }

  object StartConfig {

    def parse(args: Seq[String]): StartConfig = {
      val console = {
        args.exists(_ == "--console") ||
        Option(System.getProperty("mpc4s.http.console")).
          exists(_ equalsIgnoreCase "true")
      }

      val file = args.find(_ != "--console").
        map(f => Paths.get(f))

      StartConfig(console, file)
    }
  }
}
