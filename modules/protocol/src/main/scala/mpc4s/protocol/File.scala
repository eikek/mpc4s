package mpc4s.protocol

import java.time.Instant
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.implicits.keyvalues._
import mpc4s.protocol.codec.implicits._

sealed trait File

object File {
  case class Basic(file: String, size: Long, `Last-Modified`: Instant) extends File {
    val lastModified = `Last-Modified`
  }

  object Basic {
    implicit def codec: LineCodec[Basic] =
      cs.keyValue.exmap[Basic](fromMap, toMap).require(_.file.nonEmpty)

    def fromMap(m: ListMap[ListMap.Key, String]): Result[Basic] =
      m.as[Basic]

    def toMap(f: Basic): Result[ListMap[ListMap.Key, String]] =
      f.toStringMap
  }

  case class Directory(directory: String, `Last-Modified`: Instant) extends File {
    val lastModified = `Last-Modified`
  }

  object Directory {

    implicit def codec: LineCodec[Directory] =
      cs.keyValue.exmap[Directory](fromMap, toMap).require(_.directory.nonEmpty)

    def fromMap(m: ListMap[ListMap.Key, String]): Result[Directory] =
      m.as[Directory]

    def toMap(d: Directory): Result[ListMap[ListMap.Key, String]] =
      d.toStringMap
  }

  implicit val codec: LineCodec[File] =
    LineCodec[File].choice
}
