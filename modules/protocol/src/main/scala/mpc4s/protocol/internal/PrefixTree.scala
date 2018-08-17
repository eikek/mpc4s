package mpc4s.protocol.internal

import mpc4s.protocol.codec._

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


  def parse[A](in: String, prefixTree: List[PrefixTree[A]], all: Set[String]): Result[ParseResult[A]] = {
    def loop(current: String, tree: List[PrefixTree[A]]): Result[ParseResult[A]] = {
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
          // since we have no duplicates, there is at most one leaf node
          val leaf = many.collect({ case l: Leaf[A] => l }).headOption
          val inner = many.filter(_.isInner)
          loop(current, inner) match {
            case pr @ Right(_) => pr
            case err @ Left(_) =>
              leaf.map(l => Result.successful(ParseResult(l.value, current))).
                getOrElse(err)
          }
      }
    }

    loop(in, prefixTree)
  }



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
