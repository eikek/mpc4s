package mpc4s.protocol.codec

import syntax._
import mpc4s.protocol.codec.{codecs => cs}
sealed trait Animal

object Animal {
  case class Cat(number: Int) extends Animal
  object Cat {
    implicit val codec: LineCodec[Cat] =
      (cs.constant("cat", ()) :<>: cs.int).dropUnits.as[Cat]
  }

  case class Dog(name: String) extends Animal
  object Dog {
    implicit val codec: LineCodec[Dog] =
      (cs.constant("dog", ()) :<>: cs.charsIn('a' to 'z')).
        dropUnits.as[Dog]
  }

  case class Tiger(colored: Boolean) extends Animal
  object Tiger {
    implicit val codec: LineCodec[Tiger] =
      (cs.constant("tiger", ()) :<>: cs.boolean).
        dropUnits.as[Tiger]
  }
}
