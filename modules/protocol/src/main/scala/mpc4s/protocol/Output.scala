package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class Output(id: Id, name: String, enabled: Boolean)

object Output {

  implicit val codec: LineCodec[Output] =
    codecs.keyValue.exmap[Output](fromMap, toMap)

  def fromMap(m: ListMap[ListMap.Key, String]): Result[Output] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    m.mapKeys(s => if (s.nameLower.startsWith("output") && s.size > 6) s.map(_.substring(6)) else s).as[Output]
  }

  def toMap(s: Output): Result[ListMap[ListMap.Key, String]] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    s.toStringMap.map(_.mapKeys(k => k.map(s => "output"+ s)))
  }

}
