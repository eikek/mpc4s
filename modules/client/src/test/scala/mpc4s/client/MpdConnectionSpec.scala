package mpc4s.client

import fs2._
import cats.effect.IO
import mpc4s.protocol._
import mpc4s.protocol.commands._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._

import scala.concurrent.duration._

object MpdConnectionSpec extends MaybeTestSuite {
  val pools = ThreadPools("mpd-conn-test")
  import pools._

  val timeout = 5.seconds
  val connect = Connect.byIp("127.0.0.1", 6700)

  val disabled = true

  test("try out mpdconnection") {
    MpdConnection[IO](connect).
      flatMap(c => Stream.eval(c.requestDecode(Stats, timeout)) ++ Stream.eval(c.requestDecode(Stats, timeout))).
      evalMap(r => IO(println(s" >> '$r'"))).
      compile.drain.unsafeRunSync

    MpdConnection[IO](connect).
      evalMap(c => c.request(Status, timeout)).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdConnection[IO](connect).
      evalMap(c => c.requestDecode(Consume(false), timeout)).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdConnection[IO](connect).
      evalMap(c => c.requestDecode(ReplayGainStatus, timeout)).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    implicit val genericSelect = SelectAnswer[Stats.type, GenericAnswer]
    MpdClient[IO](connect).
      send(Stats, timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    case object WrongCommand extends Command {
      val name = CommandName("bla")
      implicit val selectAnswer = SelectAnswer[WrongCommand.type, GenericAnswer]
      implicit val codec: LineCodec[WrongCommand.type] =
        codecs.commandName(name, this)
    }
    MpdClient[IO](connect, config = CommandCodec.defaultConfig + (WrongCommand.name -> CommandName.Config[WrongCommand.type].apply)).
      send(WrongCommand, timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdClient[IO](connect).
      send(CurrentSong, timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdClient[IO](connect).
      send(PlaylistFind(Tag.Genre, "classical"), timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdClient[IO](connect).
      send(PlaylistId(None), timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdClient[IO](connect).
      send(Decoders, timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync

    MpdClient[IO](connect).
      send(UrlHandlers, timeout).
      evalMap(r => IO(println(r))).
      compile.drain.unsafeRunSync


    // endless loop in MpdConnection#197
    // MpdClient[IO](connect).
    //   send(Search(Filter(FilterType.Anywhere -> "Classical"), None, None), timeout).
    //   evalMap(r => IO(println(r))).
    //   compile.drain.unsafeRunSync
  }

  test("multiple sends, then read") {
    MpdClient[IO](connect).
      sendN(Seq[Command](Stats, Status, CurrentSong), timeout).
      evalMap(r => IO(println("'"+r+"'"))).
      compile.drain.unsafeRunSync
  }

}
