package mpc4s.client

import cats.effect.{Effect, IO}
import fs2._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicLong

import mpc4s.protocol._
import mpc4s.protocol.commands._

object MpdIdleSpec extends MaybeTestSuite {
  val pools = ThreadPools("mpd-idle-test")
  import pools._

  val disabled = true

  val timeout = 5.seconds


  test("idle") {
    val connect = Connect("127.0.0.1", 6700)

    val read = MpdClient[IO](connect).idle.
      flatMap { m =>
        val actions = everySecond[IO].
          take(6).
          evalMap({
            case 0 => m.write(CommandOrList(Status))
            case 1 => m.write(CommandOrList(Clear))
            case 2 => m.write(CommandOrList(Stats))
            case 3 => m.write(CommandOrList(ListNeighbors))
            case 4 => m.write(CommandOrList(TagTypes))
            // case 5 => m.write(CommandOrList(Ping))
            // case 6 => m.write(CommandOrList(Ping))
            case _ => IO(())
          })

        actions.concurrently(m.read.to(print))
      }

    // async.start(read.compile.drain).unsafeRunSync
    // Thread.sleep(10000)

    read.compile.drain.unsafeRunSync
  }

  def everySecond[F[_]: Effect]: Stream[F, Long] = {
    Stream.eval(Effect[F].delay(new AtomicLong(0))).
      flatMap(counter => Stream.every(1.seconds).
        filter(_ == true).
        evalMap(_ => Effect[F].delay(counter.getAndIncrement)))
  }

  def print: Sink[IO, Response[Answer]] =
    _.evalMap(ans => IO(println(s">>>> $ans")))
}
