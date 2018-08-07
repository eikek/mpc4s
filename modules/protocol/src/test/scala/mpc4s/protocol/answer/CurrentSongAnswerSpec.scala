package mpc4s.protocol.answer

import minitest._
import java.time.Instant
import mpc4s.protocol._
import mpc4s.protocol.codec._

object CurrentSongAnswerSpec extends SimpleTestSuite {

  test("codec") {
    val c = CurrentSongAnswer.codec
    val str = """file: classic/Daniel Barenboim/Beethoven For All - The Piano Concertos/cd3/02-Piano Concerto No.5 in E flat major.flac
                 |Last-Modified: 2013-12-02T00:33:39Z
                 |Album: Beethoven For All - The Piano Concertos
                 |Artist: Daniel Barenboim & Staatskapelle Berlin
                 |Genre: Classical
                 |Title: Piano Concerto No.5 in E flat major Op.73 -"Emperor" - 2. Adagio un poco mosso (Live In Bochum/2007)
                 |Date: 2012
                 |Track: 2
                 |Time: 488
                 |duration: 487.768
                 |Pos: 1
                 |Id: 68
                 |""".stripMargin

    val ans = CurrentSongAnswer(Some(
      PlaylistSong(Song(Uri("classic/Daniel Barenboim/Beethoven For All - The Piano Concertos/cd3/02-Piano Concerto No.5 in E flat major.flac")
        , Some(Instant.parse("2013-12-02T00:33:39Z"))
        , Some(Seconds(488))
        , Some(487.768)
        , ListMap(Tag.Album -> "Beethoven For All - The Piano Concertos"
          , Tag.Artist -> "Daniel Barenboim & Staatskapelle Berlin"
          , Tag.Genre -> "Classical"
          , Tag.Title -> "Piano Concerto No.5 in E flat major Op.73 -\"Emperor\" - 2. Adagio un poco mosso (Live In Bochum/2007)"
          , Tag.Date -> "2012"
          , Tag.Track -> "2"))
        , 1
        , Id("68"))))


    assertEquals(c.parse(str), Result.successful(ParseResult(ans, "")))

    // the keys are always written out in snake_case
    val strOut = """file: classic/Daniel Barenboim/Beethoven For All - The Piano Concertos/cd3/02-Piano Concerto No.5 in E flat major.flac
                 |Last-Modified: 2013-12-02T00:33:39Z
                 |time: 488
                 |duration: 487.768
                 |album: Beethoven For All - The Piano Concertos
                 |artist: Daniel Barenboim & Staatskapelle Berlin
                 |genre: Classical
                 |title: Piano Concerto No.5 in E flat major Op.73 -"Emperor" - 2. Adagio un poco mosso (Live In Bochum/2007)
                 |date: 2012
                 |track: 2
                 |pos: 1
                 |id: 68
                 |""".stripMargin
    assertEquals(c.write(ans), Result.successful(strOut))
  }
}
