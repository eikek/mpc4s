package mpc4s.protocol

import minitest._

object ListMapSpec extends SimpleTestSuite {

  test("add element") {
    val m = ListMap.empty[Int, String] + (1 -> "1") + (2 -> "2")
    assertEquals(m.toVector, Vector((1, "1"), (2, "2")))
    assertEquals(m.size, 2)

    val mr = m + (2 -> "3")
    assertEquals(mr.toVector, Vector((1, "1"), (2, "3")))
    assertEquals(mr.size, 2)
  }

  test("append") {
    val m1 = ListMap.empty[Int, String] + (1 -> "1") + (2 -> "2")
    val m2 = ListMap.empty[Int, String] + (3 -> "3") + (4 -> "4")
    assertEquals((m1 ++ m2).toVector, Vector((1,"1"), (2,"2"), (3,"3"), (4,"4")))
    assertEquals(m1 ++ ListMap.empty[Int, String], m1)
    assertEquals(ListMap.empty[Int, String] ++ m1, m1)
  }

  test("reverse") {
    val m = ListMap.empty[Int, String] + (1 -> "1") + (2 -> "2")
    assertEquals(m.reverse.toVector, Vector((2, "2"), (1, "1")))
  }

  test("mapKeys") {
    val m = ListMap.empty[Int, String] + (1 -> "1") + (2 -> "2")
    val mapped = m.mapKeys(_ + 1)
    assertEquals(mapped.size, 2)
    assertEquals(mapped.toVector, Vector((2, "1"), (3 -> "2")))
    assertEquals(mapped.mapKeys(_ - 1), m)
  }
}
