package mpc4s.protocol.answer

import minitest._
import mpc4s.protocol._
import mpc4s.protocol.codec._

object StatusAnswerSpec extends SimpleTestSuite {

  test("codec") {
    val c = StatusAnswer.codec

    val ans1 = StatusAnswer(100,false,false,SingleState.Off,false,54,12,PlayState.Stop
      ,None,None,None,None,None,None,None,None,None,Some(0.0),None,Some(""),Some(""))
    val str1 = """volume: 100
                |repeat: 0
                |random: 0
                |single: 0
                |consume: 0
                |playlist: 54
                |playlistlength: 12
                |mixrampdb: 0.000000
                |state: stop
                |""".stripMargin

    val ans2 = StatusAnswer(100,false,false,SingleState.Off,false,54,12,PlayState.Pause
      , Some(0),Some(Id("20")),Some(1),Some(Id("21")),Some(Range(33,160))
      ,Some(32.554),Some(159.817),Some(826),None,Some(0.0)
      ,Some(AudioFormat(44100,16,2)),Some(""),Some(""))
    val str2 = """volume: 100
                 |repeat: 0
                 |random: 0
                 |single: 0
                 |consume: 0
                 |playlist: 54
                 |playlistlength: 12
                 |mixrampdb: 0.000000
                 |state: pause
                 |song: 0
                 |songid: 20
                 |time: 33:160
                 |elapsed: 32.554
                 |bitrate: 826
                 |duration: 159.817
                 |audio: 44100:16:2
                 |nextsong: 1
                 |nextsongid: 21
                 |""".stripMargin

    assertEquals(c.parse(str2), Result.successful(ParseResult(ans2, "")))
    assertEquals(c.parse(str1), Result.successful(ParseResult(ans1, "")))
  }
}
