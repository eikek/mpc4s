package mpc4s.protocol.commands

import minitest._
import mpc4s.protocol.codec._
import mpc4s.protocol._

object SeekCurSpec extends SimpleTestSuite {

  test("codec") {
    val c = SeekCur.codec

    assertEquals(c.parseValue("seekcur -12"), Result.successful(SeekCur(Relation.Negative, Seconds(12))))
    assertEquals(c.parseValue("seekcur +12"), Result.successful(SeekCur(Relation.Positive, Seconds(12))))
    assertEquals(c.parseValue("seekcur 12"), Result.successful(SeekCur(Relation.Absolute, Seconds(12))))
    assertEquals(c.write(SeekCur(Relation.Positive, Seconds(24))), Result.successful("seekcur +24"))
    assertEquals(c.write(SeekCur(Relation.Negative, Seconds(24))), Result.successful("seekcur -24"))
    assertEquals(c.write(SeekCur(Relation.Absolute, Seconds(24))), Result.successful("seekcur 24"))
  }
}
