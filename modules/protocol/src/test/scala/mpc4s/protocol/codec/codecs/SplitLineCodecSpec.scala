package mpc4s.protocol.codec.codecs

import minitest._
import mpc4s.protocol._
import mpc4s.protocol.codec._

object SplitLineCodecSpec extends SimpleTestSuite {

  def numRange(start: Int, end: Int) =
    scala.Range(start, end + 1)

  test("splitSongs") {
    val c = SplitLineCodec.splitSongs
    val body = (n: Int) => s"file: usb/music/test${n}.flac\ntime: 154\nduration: 154.11\n"

    for (n <- 1 to 5) {
      val str = numRange(1, n).map(body).mkString
      assertEquals(c.parse(str), Result.successful(ParseResult(numRange(1, n).map(body).toVector, "")))
    }
  }

  test("splitTags") {
    val c = SplitLineCodec.splitTag
    val body = "songs: 12\nplaytime: 12154\n"

    for (n <- 1 to 5) {
      Tag.all.foreach { tag =>
        val el: Int => String = i => s"${tag.name}: Test${i}\n" + body
        val str = numRange(1, n).map(el).mkString
        assertEquals(c.parse(str), Result.successful(ParseResult(numRange(1, n).map(el).toVector, "")))
      }
    }
  }

  test("splitTag when no tag") {
    val c = SplitLineCodec.splitTag

    assertEquals(c.parse("songs: 12\nplaytime: 12154\n"),
      Result.successful(ParseResult(Vector.empty, "songs: 12\nplaytime: 12154\n")))
  }

  //TODO tests for write
}
