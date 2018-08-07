package mpc4s.protocol.internal

import minitest._
import mpc4s.protocol.internal.PrefixTree._

object EnumSpec extends SimpleTestSuite {

  test("simple test") {
    val tree = PrefixTree(List(
      ("artist", 1),
      ("album", 2),
      ("disc", 3),
      ("albumartist", 4)
    ))

    assertEquals(tree,
      List(Node("disc",List(Leaf(3)))
        , Node("a",List(Node("rtist",List(Leaf(1))), Node("lbum",List(Leaf(2), Node("artist",List(Leaf(4)))))))))

  }
}
