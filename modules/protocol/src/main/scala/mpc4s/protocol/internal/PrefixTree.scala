package mpc4s.protocol.internal

sealed trait PrefixTree[A] {
  def isLeaf: Boolean
  def isInner: Boolean = !isLeaf
}

object PrefixTree {

  case class Node[A](prefix: String, nodes: List[PrefixTree[A]]) extends PrefixTree[A] {
    val isLeaf = false
  }

  case class Leaf[A](value: A) extends PrefixTree[A] {
    val isLeaf = true
  }

  def apply[A](list: List[(String, A)]): List[PrefixTree[A]] =
    groupPrefix(list, Node("", Nil)).nodes

  def from[E <: Enum](list: List[E]): List[PrefixTree[E]] =
    apply(list.map(e => (e.name, e)))

  private def groupPrefix[A](list: List[(String, A)], node: Node[A]): Node[A] = {
    val children = list.groupBy(_._1.take(1)).
      map( { case (n, list) =>
        val leafs = list.filter(_._1.length == 1)
        val inner = list.filter(_._1.length > 1)
        val next = Node(n, leafs.map(t => Leaf(t._2)))
        groupPrefix(inner.map(t => (t._1.drop(1), t._2)), next)
      })

    compact(node.copy(nodes = node.nodes ::: children.toList)).asInstanceOf[Node[A]]
  }

  private def compact[A](tree: PrefixTree[A]): PrefixTree[A] =
    tree match {
      case cur @ Node(name, nodes) =>
        nodes match {
          case (n @ Node(_, _)) :: Nil =>
            Node(name + n.prefix, n.nodes.map(compact))
          case _ =>
            cur
        }
      case l: Leaf[A] => l
    }
}
