package mpc4s.protocol

import mpc4s.protocol.codec._

case class Decibel(n: Double)

object Decibel {

  implicit def codec(implicit ic: LineCodec[Double]): LineCodec[Decibel] =
    ic.xmap(Decibel.apply, _.n)
}
