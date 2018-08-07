package mpc4s.protocol

/** This package contains codecs for commands and answers. Since mpd
  * uses a simple protocol based on UTF8 encoded lines, the codecs use
  * this as its target (in contrast to bytes).
  */
package codec {
}

package object codec {

  type ProtocolConfig = Map[CommandName, CommandName.Config]

  type Result[A] = Either[ErrorMessage, A]

  object Result {

    def attempt[A](a: => A, err: String = ""): Result[A] =
      try {
        Right(a)
      } catch {
        case util.control.NonFatal(ex) =>
          if (err == "") Left(ErrorMessage(ex.getMessage))
          else Left(ErrorMessage(err))
      }

    def failure[A](err: ErrorMessage): Result[A] =
      Left(err)

    def successful[A](a: A): Result[A] = Right(a)

    def flatten[A](rs: Vector[Result[A]]): Result[Vector[A]] =
      rs.foldRight(Result.successful(Vector.empty[A])){ (next, rseq) =>
        next match {
          case Right(a) => rseq.map(l => a +: l)
          case Left(_) => next.asInstanceOf[Result[Vector[A]]]
        }
      }
  }
}
