package mpc4s.protocol

import minitest._

object TagSpec extends SimpleTestSuite {

  test("from tuples") {
    val data = Vector("artist" -> "Heifetz", "album" -> "Chaconne")
    assertEquals(Tag.fromStrings(data), ListMap(Tag.Artist -> "Heifetz", Tag.Album -> "Chaconne"))

    val data2 = Vector("album" -> "Chaconne", "albumartist" -> "Heifetz")
    assertEquals(Tag.fromStrings(data2), ListMap(Tag.Album -> "Chaconne", Tag.Albumartist -> "Heifetz"))
  }
}
