package mpc4s.protocol.internal

import mpc4s.protocol.codec._

trait Enum {
  protected def productPrefix: String

  val name: String = Case.camelToSnake(productPrefix)

}

object Enum {

  def codecFromAll[E <: Enum](all: List[E]): LineCodec[E] = {
   new EnumCodec(all)
    // val cs = all.map(e => codecs.constant(e.name.toLowerCase, e))
    // codecs.choice(cs.head, cs.tail: _*)
  }


   class EnumCodec[E <: Enum](all: List[E]) extends LineCodec[E] {
    private val names: Set[String] = all.map(_.name.toLowerCase).toSet

    private val enumTree = PrefixTree.from(all)

    def write(in: E): Result[String] =
      if (names.contains(in.name.toLowerCase)) Result.successful(in.name)
      else Result.failure(ErrorMessage(s"Enum '$in' not in expected values '$names'"))

    def parse(in: String): Result[ParseResult[E]] = {
      import PrefixTree._

      def loop(current: String, tree: List[PrefixTree[E]]): Result[ParseResult[E]] = {
        val cnd = tree.filter({
          case PrefixTree.Node(name, _) =>
            Case.startsWithIgnoreCase(name, current)
          case PrefixTree.Leaf(en) =>
            true
        })

        cnd match {
          case Nil =>
            Result.failure(ErrorMessage(s"Expected constant from '${all.mkString(", ")}'; bot got '$current'"))

          case Leaf(c) :: Nil =>
            Result.successful(ParseResult(c, current))

          case Node(name, more) :: Nil =>
            loop(current.substring(name.length), more)

          case many =>
            // since enums have no duplicates, there is at most one leaf node
            val leaf = many.collect({ case l: Leaf[E] => l }).headOption
            val inner = many.filter(_.isInner)
            loop(current, inner) match {
              case pr @ Right(_) => pr
              case err @ Left(_) =>
                leaf.map(l => Result.successful(ParseResult(l.value, current))).
                  getOrElse(err)
            }
        }
      }

      loop(in, enumTree)
    }
  }
}
