package mpc4s.protocol.answer

import minitest._
import mpc4s.protocol._
import mpc4s.protocol.codec._

object StatsAnswerSpec extends SimpleTestSuite {

  test("codec") {
    val c = StatsAnswer.codec

    val ans = StatsAnswer(2,12,88, Seconds(1212), 545, 55, 21212)
    val str = """artists: 2
                |albums: 12
                |songs: 88
                |uptime: 1212
                |db_playtime: 545
                |db_update: 55
                |playtime: 21212
                |""".stripMargin

    assertEquals(c.write(ans), Right(str))
    assertEquals(c.parse(str), Right(ParseResult(ans, "")))
  }
}
