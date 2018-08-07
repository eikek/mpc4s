package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class FileList(songs: Vector[File]) {
  def isEmpty: Boolean = songs.isEmpty

  def nonEmpty: Boolean = songs.nonEmpty

}

object FileList {
  val Empty = FileList(Vector.empty)

  implicit def codec(implicit pc: LineCodec[File]): LineCodec[FileList] = {
    val existing: LineCodec[FileList] =
      cs.map(cs.splitFiles, (pc :: cs.empty).dropUnits.head).
        xmap(FileList.apply, _.songs)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
