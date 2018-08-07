package mpc4s.protocol.answer

import minitest._
import mpc4s.protocol._
import mpc4s.protocol.codec._

object IdleAnswerSpec extends SimpleTestSuite {

  test("codec") {
    val c = IdleAnswer.codec

    val idleAnswer = IdleAnswer(List(ChangeEvent(Subsystem.Database), ChangeEvent(Subsystem.Mixer)))
    val str = c.write(idleAnswer)
    assertEquals(str, Result.successful("changed: database\nchanged: mixer\n"))
    assertEquals(c.parse("changed: database\nchanged: mixer\n"), Result.successful(ParseResult(idleAnswer, "")))
  }
}
