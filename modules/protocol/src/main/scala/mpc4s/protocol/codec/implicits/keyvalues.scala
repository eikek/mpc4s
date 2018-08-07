package mpc4s.protocol.codec.implicits

import mpc4s.protocol.codec._

/** When deriving case classes from key-value lines, a string codec is
  * necessary to be in scope. This should be the identity codec. If
  * the case class contains String fields, this import is required.
  */
object keyvalues extends codecs.CaseClassCodec {

  implicit val stringCodec: LineCodec[String] =
    codecs.rest

}
