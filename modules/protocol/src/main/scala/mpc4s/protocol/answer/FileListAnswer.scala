package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class FileListAnswer(files: FileList) extends Answer

object FileListAnswer {

  implicit def codec(implicit fc: LineCodec[FileList]): LineCodec[FileListAnswer] =
    fc.xmap(FileListAnswer.apply, _.files)
}
