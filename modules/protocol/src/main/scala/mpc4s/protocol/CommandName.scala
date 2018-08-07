package mpc4s.protocol

import mpc4s.protocol.codec._

sealed trait CommandName {
  def name: String

  final def /(next: String): CommandName = CommandName.Path(this, next)

  final lazy val path: List[String] = {
    val r = this match {
      case CommandName.Root => Nil
      case CommandName.Path(p, n) => n :: p.path
    }
    r.reverse
  }
}

object CommandName {

  trait Config {
    type Cmd <: Command
    type Ans <: Answer

    def commandCodec: LineCodec[Cmd]
    def responseCodec: LineCodec[Response[Ans]]
  }

  object Config {
    trait Select[C <: Command] {
      def apply[A <: Answer](implicit cc: LineCodec[C], select: SelectAnswer[C, A]): Config =
        Config[C,A](cc, select.codec)
    }

    def apply[C <: Command]: Select[C] = new Select[C]{}


    def apply[C <: Command, A <: Answer](cc: LineCodec[C], rc: LineCodec[Response[A]]): Config =
      new Config {
        type Cmd = C
        type Ans = A
        val responseCodec = rc
        val commandCodec = cc
      }

  }

  case object Root extends CommandName { val name = "" }

  case class Path(parent: CommandName, name: String) extends CommandName

  def apply(name: String, more: String*): CommandName =
    (name :: more.toList).foldLeft(Root: CommandName)(_ / _)


  def find(names: Set[CommandName], str: String): Option[CommandName] = {
    @annotation.tailrec
    def loop(ns: Set[CommandName], words: List[String], element: Int): Option[CommandName] = 
      words match {
        case Nil =>
          val set = ns.filter(_.path.size == element)
          if (set.isEmpty || set.size == 1) set.headOption
          else None
        case word :: ws =>
          val set = ns.filter(_.path.lift(element) == Option(word))
          if (set.isEmpty || set.size == 1) set.headOption
          else loop(set, ws, element + 1)
      }

    loop(names, str.split("\\s+").toList, 0)
  }

}
