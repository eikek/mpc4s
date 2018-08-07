package mpc4s.protocol.codec.codecs

import shapeless._
import shapeless.labelled._
import mpc4s.protocol._
import mpc4s.protocol.codec._
//import mpc4s.protocol.internal._

trait CaseClassCodec {
  // learned from https://stackoverflow.com/questions/31633563/converting-nested-case-classes-to-nested-maps-using-shapeless#31638390
  trait Encode[L <: HList] {
    def apply(l: L): Result[ListMap[ListMap.Key, String]]
  }
  trait Decode[L <: HList] {
    def apply(l: ListMap[ListMap.Key, String]): Result[L]
  }

  implicit val hnilToEmpty: Encode[HNil] =
    new Encode[HNil] {
      def apply(l: HNil) = Result.successful(ListMap.empty)
    }

  implicit def hconsEncode1[K <: Symbol, V, T <: HList](implicit
    wit: Witness.Aux[K],
    encT: Encode[T],
    sc: LineCodec[V]
  ): Encode[FieldType[K, V] :: T] = new Encode[FieldType[K, V] :: T] {
    def apply(l: FieldType[K, V] :: T): Result[ListMap[ListMap.Key, String]] = {
      for {
        r <- encT(l.tail)
        h <- sc.write(l.head)
      } yield if (h == "") r else (r + (ListMap.key(wit.value.name) -> h))
    }
  }


  implicit val emptyToHHil: Decode[HNil] =
    new Decode[HNil] {
      def apply(m: ListMap[ListMap.Key, String]) = Result.successful(HNil)
    }

  implicit def mapToHCons[K <: Symbol, V, T <: HList](implicit
    wit: Witness.Aux[K],
    decT: Decode[T],
    sc: LineCodec[V]
  ): Decode[FieldType[K,V] :: T] = new Decode[FieldType[K,V] :: T] {

    def apply(m: ListMap[ListMap.Key, String]): Result[FieldType[K,V] :: T] = {
      val key = ListMap.key(wit.value.name)
      val value = m.get(key).getOrElse("")
      val decoded = sc.parse(value).map(_.value).left.
        map(err => ErrorMessage(s"Error decoding value '${m.get(key)}' for '${wit.value.name}': ${err.message}. Map: $m"))
      for {
        rest <- decT(m)
        h    <- decoded
      } yield field[K](h) :: rest
    }
  }

  final class Builder[A] {
    def fromMap[L <: HList](m: ListMap[ListMap.Key,String])(implicit
      gen: LabelledGeneric.Aux[A, L],
      decR: Decode[L]
    ): Result[A] = decR(m).map(x => gen.from(x))
  }


  implicit final class EncodeOps[A](a: A) {
    def toStringMap[L <: HList](implicit
      gen: LabelledGeneric.Aux[A, L],
      tmr: Encode[L]
    ): Result[ListMap[ListMap.Key, String]] = tmr(gen.to(a)).map(_.reverse)
  }

  implicit final class DecodeOps[L <: HList](m: ListMap[ListMap.Key, String]) {

    def as[A](implicit gen: LabelledGeneric.Aux[A, L], decR: Decode[L]): Result[A] =
      new Builder[A].fromMap(m)
  }

}

object CaseClassCodec extends CaseClassCodec 
