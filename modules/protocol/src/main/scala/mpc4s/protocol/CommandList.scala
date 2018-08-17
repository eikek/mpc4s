package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

/** Allows to pass many commands at once to mpd using the
  * `command_list_begin` feature.
  *
  * Note: currently this is not completely implemented. Only the last
  * command may return a non-empty response. All other commands in
  * this list must be commands that expect an empty answer.
  */
final case class CommandList(all: Vector[Command]) {

  def isEmpty: Boolean = all.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def names: Vector[CommandName] =
    all.map(_.name)
}

object CommandList {

  def apply(cs: Command*): CommandList =
    CommandList(cs.toVector)

  implicit def codec(implicit cc: LineCodec[Command]): LineCodec[CommandList] = {
    val nl = cs.constant("\n", ())
    val listBegin = cs.constant("command_list_begin\n", ())
    val listEnd = cs.constant("command_list_end\n", ())

    val cl: LineCodec[CommandList] =
      (cc :: nl).dropUnits.head.repeat.
        xmap[CommandList](CommandList.apply, _.all)

    (listBegin :: cl :: listEnd).dropUnits.head
  }
}
