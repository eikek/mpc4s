package mpc4s.client

import fs2._

object ResponseSplit {

  private val nl = '\n'.toByte
  private val O = 'O'.toByte
  private val K = 'K'.toByte
  private val A = 'A'.toByte
  private val C = 'C'.toByte

  def responsesUtf8[F[_]](bufferSize: Int = 32 * 1024): Pipe[F, Byte, String] =
    s => s.through(responses(bufferSize))

  def responses[F[_]](bufferSize: Int = 32 * 1024): Pipe[F, Byte, String] = {
    def go(buffer: Array[Byte], s: Stream[F, Byte]): Pull[F, String, Option[Unit]] =
      s.pull.unconsChunk.flatMap {
        case Some((chunk, rest)) =>
          val (resp, newBuffer) = extractResponses(buffer, chunk, bufferSize)
          if (resp.nonEmpty) Pull.outputChunk(Chunk.vector(resp)) >> go(newBuffer, rest)
          else go(newBuffer, rest)

        case None if buffer.nonEmpty =>
          Pull.output1(new String(buffer)) >> Pull.pure(None)

        case None =>
          Pull.pure(None)
      }

    s => go(new Array[Byte](0), s).stream
  }

  // check on each byte, if full response is reached: that is, either
  // single line starting with ACK or anything else ending with OK\n.
  //
  // if not a full response, examine the next chunk and concatenate on
  // first 'OK\n' or if nothing is found again.

  def extractResponses(searched: Array[Byte], bv: Chunk[Byte], bufferSize: Int): (Vector[String], Array[Byte]) = {
    val out = new java.io.ByteArrayOutputStream(bufferSize)

    var appended: Boolean = false
    var resp: Vector[String] = Vector.empty

    var b0: Byte = 0
    var b1: Byte = 0
    var a0: Int = if (searched.length > 0) searched(0).toInt else Int.MinValue
    var a1: Int = if (searched.length > 1) searched(1).toInt else Int.MinValue
    var a2: Int = if (searched.length > 2) searched(2).toInt else Int.MinValue
    bv.foreach(b => {
      if (a0 == Int.MinValue) {
        a0 = b.toInt
      } else {
        if (a1 == Int.MinValue) {
          a1 = b.toInt
        } else if (a2 == Int.MinValue) {
          a2 = b.toInt
        }
      }

      out.write(b.toInt)
      if (b == nl) {
        if ((b1 == K && b0 == O) || (a0 == A && a1 == C && a2 == K)) {
          if (appended) {
            resp = resp :+ new String(out.toByteArray, "UTF-8")
          } else {
            val sout = concat(searched, out.toByteArray)
            resp = resp :+ new String(sout, "UTF-8")
            appended = true
          }
          out.reset()
          a0 = Int.MinValue
          a1 = Int.MinValue
          a2 = Int.MinValue
        }
      }

      b0 = b1
      b1 = b
    })

    if (resp.isEmpty) (resp, concat(searched, out.toByteArray))
    else (resp, out.toByteArray)
  }

  final def concat(a1: Array[Byte], a2: Array[Byte]): Array[Byte] = {
    if (a1.length == 0) a2
    else if (a2.length == 0) a1
    else {
      val len = a1.length + a2.length
      val result = new Array[Byte](len)
      System.arraycopy(a1, 0, result, 0, a1.length)
      System.arraycopy(a2, 0, result, a1.length, a2.length)
      result
    }
  }
}
