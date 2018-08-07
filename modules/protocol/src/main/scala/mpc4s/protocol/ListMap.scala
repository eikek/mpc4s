package mpc4s.protocol

/** Immutable map that keeps insertion order.
  */
sealed trait ListMap[A,B] extends Iterable[(A,B)] {

  def get(a: A): Option[B]

  def +(t: (A,B)): ListMap[A,B]

  def ++(next: ListMap[A,B]): ListMap[A,B]

  def reverse: ListMap[A,B]

  def toVector: Vector[(A,B)]

  def isEmpty: Boolean

  def nonEmpty: Boolean

  def mapKeys[C](f: A => C): ListMap[C,B]

  def toMap: Map[A,B]

}

object ListMap {

  def apply[A,B](t: (A,B)*): ListMap[A,B] =
    ListMap.from(t)

  def empty[A,B]: ListMap[A,B] =
    new ListMapImpl(Map.empty, Vector.empty)

  def from[A,B](v: TraversableOnce[(A,B)]): ListMap[A,B] =
    new ListMapImpl(v.toMap, v.map(_._1).toVector)


  final class Key(val name: String) {
    val nameLower = name.toLowerCase

    def map(f: String => String): Key =
      new Key(f(name))

    val size = name.length

    override def equals(any: Any): Boolean =
      any match {
        case k: Key => k.nameLower == nameLower
        case _ => false
      }

    override def hashCode(): Int =
      nameLower.hashCode

    override def toString(): String =
      s"Key($name)"
  }

  def key(name: String): Key =
    new Key(name)

  def fromStrings(v: TraversableOnce[(String,String)]): ListMap[Key,String] =
    from(v).mapKeys(new Key(_))

  def strings(t: (String,String)*): ListMap[Key,String] =
    apply(t.map(p => (key(p._1), p._2)): _*)


  private final class ListMapImpl[A,B](val m: Map[A,B], val order: Vector[A]) extends ListMap[A,B] {
    val toMap = m
    def get(a: A) = m.get(a)

    def +(t: (A,B)): ListMap[A,B] =
      if (m.contains(t._1)) new ListMapImpl(m + t, order)
      else new ListMapImpl(m + t, order :+ t._1)

    def ++(next: ListMap[A,B]): ListMap[A,B] =
      next.foldLeft(this: ListMap[A,B])(_ + _)

    def reverse: ListMap[A,B] =
      new ListMapImpl(m, order.reverse)

    def mapKeys[C](f: A => C): ListMap[C,B] =
      new ListMapImpl(m.map(t => f(t._1) -> t._2), order.map(f))

    def iterator: Iterator[(A,B)] =
      new Iterator[(A,B)] {
        private val iter = order.iterator

        def hasNext: Boolean = iter.hasNext
        def next = {
          val a = iter.next
          a -> m(a)
        }
      }

    override def size: Int = order.size

    override def equals(other: Any): Boolean =
      other match {
        case lm: ListMapImpl[_,_] => m == lm.m && order == lm.order
        case _ => false
      }

    override def hashCode(): Int =
      m.hashCode + (13 * order.hashCode)

    override def toString() =
      s"ListMap($m)"
  }
}
