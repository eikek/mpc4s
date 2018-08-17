package mpc4s.protocol.internal

import mpc4s.protocol.codec._

trait Enum {
  protected def productPrefix: String

  val name: String = Case.camelToSnake(productPrefix)

}

object Enum {

  def codecFromAll[E <: Enum](all: List[E]): LineCodec[E] = {
   new EnumCodec(all)
  }

  class EnumCodec[E <: Enum](all: List[E]) extends LineCodec[E] {
    private val names: Set[String] = all.map(_.name.toLowerCase).toSet

    private val enumTree = PrefixTree.from(all)

    def write(in: E): Result[String] =
      if (names.contains(in.name.toLowerCase)) Result.successful(in.name)
      else Result.failure(ErrorMessage(s"Enum '$in' not in expected values '$names'"))

    def parse(in: String): Result[ParseResult[E]] = {
      PrefixTree.parse(in, enumTree, names)
    }
  }
}
