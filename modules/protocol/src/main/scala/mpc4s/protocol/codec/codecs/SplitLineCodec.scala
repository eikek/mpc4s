package mpc4s.protocol.codec.codecs

import scala.util.matching.Regex
import mpc4s.protocol._
import mpc4s.protocol.codec._

private final class SplitLineCodec(starting: Regex) extends LineCodec[Vector[String]] {

  def write(in: Vector[String]): Result[String] = {
    in.foldRight(Result.successful(in.mkString)) { (str, res) =>
      if (starting.findPrefixOf(str).isDefined) res
      else Result.failure(ErrorMessage(s"Element '$str' does not start with regex '$starting'"))
    }
  }

  def parse(in: String): Result[ParseResult[Vector[String]]] = {
    val (last, v) = starting.findAllMatchIn(in).
      foldLeft((0, Vector.empty[String])) { (t, m) =>
        val (i, v) = t
        (m.start, v :+ in.substring(i, m.start))
      }
    if (v.isEmpty) Result.successful(ParseResult(v, in))
    else if (v.head.nonEmpty) Result.failure(ErrorMessage(s"Input '$in' not starting with regex '$starting'"))
    else {
      val res =
        if (last < in.length) v.tail :+ in.substring(last)
        else v.tail
      Result.successful(ParseResult(res, ""))
    }
  }
}

object SplitLineCodec {
  def split(starting: Regex): LineCodec[Vector[String]] =
    new SplitLineCodec(starting)

  // the split puts the newline at the beginning of a chunk
  // but it needs to be at the end of the previous
  private def shiftPrev(in: Vector[String]): Vector[String] = {
    val char = '\n'
    val r = in.foldRight(("", Vector.empty[String])) { (el, t) =>
      val (suffix, v) = t
      if (el.charAt(0) == char) (el.charAt(0).toString, (el.substring(1) + suffix) +: v)
      else ("", (el + suffix) +: v)
    }
    r._2
  }

  // the inverse to shiftPrev
  private def shiftNext(in: Vector[String]): Vector[String] = {
    val r = in.foldLeft(("", Vector.empty[String])) { (t, el) =>
      val (pref, v) = t
      if (el.last == '\n') (el.last.toString, v :+ (pref + el.dropRight(1)))
      else ("", v :+ (pref + el))
    }
    if (r._2.isEmpty) r._2
    else r._2.init :+ r._2.last + '\n'
  }

  // the splitting must take the newline char, as it otherwise could
  // split at unwanted places (like when a song name contains
  // `file:`).

  def splitSongs: LineCodec[Vector[String]] =
    split("(?i)(^|\n)file:".r).
      xmap(shiftPrev, shiftNext)

  def splitPlaylists: LineCodec[Vector[String]] =
    split("(?i)(^|\n)playlist:".r).
      xmap(shiftPrev, shiftNext)

  def splitTag: LineCodec[Vector[String]] = {
    val regex = ("(?i)(^|\n)(" + Tag.all.map(_.name).mkString("|") +"):").r
    split(regex).xmap(shiftPrev, shiftNext)
  }

  def splitFiles: LineCodec[Vector[String]] = {
    val regex = "(?i)(^|\n)(file|directory|playlist):".r
    split(regex).xmap(shiftPrev, shiftNext)
  }

  def splitOutputs: LineCodec[Vector[String]] = {
    val regex = "(?i)(^|\n)outputid:".r
    split(regex).xmap(shiftPrev, shiftNext)
  }

  def splitDecoderPlugins: LineCodec[Vector[String]] = {
    val regex = "(?i)(^|\n)plugin:".r
    split(regex).xmap(shiftPrev, shiftNext)
  }
}
