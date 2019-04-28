package mpc4s.http.internal

import scodec.{Err, Attempt, Codec}
import scodec.codecs._
import spinoco.protocol.http.header._
import spinoco.protocol.http.codec.{HttpHeaderCodec => SpinocoCodec}

object HeaderCompat {

  /**
    * Wraps all header codecs in
    * `spinoco.protocol.http.codec.HttpHeaderCodec` to allow empty
    * values.
    *
    */
  def codec(maxHeaderLength: Int, otherHeaders: (String, Codec[HttpHeader]) *):Codec[HttpHeader] = {
    val all = allCodecs ++
      otherHeaders.map { case (hdr,codec) => hdr.toLowerCase -> choice(codec, emptyHeader(hdr.toLowerCase)) }.toMap
    SpinocoCodec.codec(maxHeaderLength, all.toSeq: _*)
  }


  val emptyString: Codec[Unit] = {
    ascii.exmap(
      s => if (s.trim.isEmpty) Attempt.successful(()) else Attempt.failure(Err("Expected end of input")),
      _ => Attempt.successful("")
    )
  }


  def emptyHeader(name: String): Codec[HttpHeader] =
    recover(emptyString).exmap(
      b => Attempt.successful(GenericHeader(name, ""))
      , _ => Attempt.successful(true))

  val allCodecs = SpinocoCodec.allHeaderCodecs.
    map { case (name, codec) => name -> choice(codec, emptyHeader(name)) }

}
