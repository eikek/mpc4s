package mpc4s.http.util

import io.circe._
import Size._
import scala.util.Try

sealed abstract class Size {
  def toBytes: Long
  def bytes: Int = toBytes.toInt
  def asString: String

  def + (other: Size): Size =
    Bytes(toBytes + other.toBytes)

  def - (other: Size): Size =
    Bytes(toBytes - other.toBytes)

  def > (other: Size): Boolean =
    toBytes > other.toBytes

  def >= (other: Size): Boolean =
    toBytes >= other.toBytes

  def < (other: Size): Boolean =
    toBytes < other.toBytes

  def <= (other: Size): Boolean =
    toBytes <= other.toBytes

  def asBytes: Bytes = Bytes(toBytes)
  def asKBytes: KBytes
  def asMBytes: MBytes
  def asGBytes: GBytes

  override def equals(o: Any): Boolean =
    o match {
      case sz: Size => Size.equals(this, sz)
      case _ => false
    }
}

object Size {
  val zero: Size = Bytes(0L)

  def equals(s1: Size, s2: Size): Boolean =
    s1.toBytes == s2.toBytes

  implicit val _sizeDec: Decoder[Size] = Decoder.decodeLong.map(b => Bytes(b))
  implicit val _sizeEnc: Encoder[Size] = Encoder.encodeLong.contramap[Size](_.toBytes)

  private def format(d: Double) = "%.2f".formatLocal(java.util.Locale.ROOT, d)

  def parse(str: String): Option[Size] =
    Try(str.toLowerCase.last match {
      case 'k' => KBytes(str.dropRight(1).toDouble)
      case 'm' => MBytes(str.dropRight(1).toDouble)
      case 'g' => GBytes(str.dropRight(1).toDouble)
      case _ => Bytes(str.toLong)
    }).toOption

  final case class Bytes(value: Long) extends Size {
    def toBytes = value

    override def asBytes: Bytes = this
    def asKBytes: KBytes = KBytes(value / 1024.0)
    def asMBytes: MBytes = MBytes(asKBytes.value / 1024.0)
    def asGBytes: GBytes = GBytes(asMBytes.value / 1024.0)
    def asString =
      if (value < 1024) s"${value}B"
      else KBytes(value / 1024.0).asString
  }

  final case class KBytes(value: Double) extends Size {
    def toBytes = (value * 1024).toLong

    def asKBytes: KBytes = this
    def asMBytes: MBytes = MBytes(value / 1024.0)
    def asGBytes: GBytes = GBytes(asMBytes.value / 1024.0)

    def asString =
      if (value < 1024) s"${Size.format(value)}K"
      else MBytes(value / 1024.0).asString
  }

  final case class MBytes(value: Double) extends Size {
    def toBytes = (value * 1024 * 1024).toLong

    def asKBytes: KBytes = KBytes(value * 1024.0)
    def asMBytes: MBytes = this
    def asGBytes: GBytes = GBytes(value / 1024.0)

    def asString =
      if (value < 1024) s"${Size.format(value)}M"
      else GBytes(value / 1024.0).asString
  }

  final case class GBytes(value: Double) extends Size {
    def toBytes = (value * 1024 * 1024 * 1024).toLong

    def asKBytes: KBytes = KBytes(asMBytes.value * 1024.0)
    def asMBytes: MBytes = MBytes(value * 1024.0)
    def asGBytes: GBytes = this

    def asString = s"${Size.format(value)}G"
  }

  object Implicits {
    implicit final class IntSizeOps(val n: Int) extends AnyVal {
      def gbytes: Size = GBytes(n.toDouble)
      def mbytes: Size = MBytes(n.toDouble)
      def kbytes: Size = KBytes(n.toDouble)
      def bytes: Size = Bytes(n.toLong)
    }
    implicit final class LongSizeOps(val n: Long) extends AnyVal {
      def gbytes: Size = GBytes(n.toDouble)
      def mbytes: Size = MBytes(n.toDouble)
      def kbytes: Size = KBytes(n.toDouble)
      def bytes: Size = Bytes(n)
    }
  }
}
