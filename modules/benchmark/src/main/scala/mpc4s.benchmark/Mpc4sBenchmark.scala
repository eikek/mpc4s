package mpc4s.benchmark

import fs2.{text, Stream}
import cats.effect.IO
import org.openjdk.jmh.annotations._
import mpc4s.protocol._
import mpc4s.protocol.codec.codecs._
import mpc4s.protocol.answer._
import mpc4s.client._

@State(Scope.Thread)
class Mpc4sBenchmark {

  def songlistStream: Stream[IO, Byte] =
    readResource("/songlist.txt", 32 * 1024)

  val songlist =
    makeString(songlistStream)

  def albumStream: Stream[IO, Byte] =
    readResource("/album.txt", 32 * 1024)

  val album =
    makeString(albumStream)


  @Benchmark
  def songlistResponseParse(): Unit = {
    val codec = Response.createCodec(SongListAnswer.codec)
    codec.parseFull(songlist)
    ()
  }

  @Benchmark
  def albumResponseParse(): Unit = {
    val codec = Response.createCodec(ListAnswer.codec)
    codec.parseFull(album)
    ()
  }

  @Benchmark
  def responseSplit(): Unit = {
    (albumStream ++ songlistStream).
      through(ResponseSplit.responsesUtf8[IO]()).
      compile.toVector.
      unsafeRunSync
    ()
  }

  @Benchmark
  def splitCodec(): Unit = {
    val codec = SplitLineCodec.splitSongs
    codec.parseFull(songlist)
    ()
  }

  private def readResource(name: String, chunkSize: Int): Stream[IO, Byte] = {
    val in = getClass.getResourceAsStream(name)
    if (in == null) sys.error("no resoure: "+ name)

    fs2.io.readInputStream(IO(in), chunkSize, true)
  }

  private def makeString(in: Stream[IO, Byte]): String = {
    in.
      through(text.utf8Decode).
      fold1(_ ++ _).
      compile.last.
      unsafeRunSync.
      getOrElse(sys.error("no content in stream"))
  }
}
