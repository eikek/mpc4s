package mpc4s.client

import cats.effect.Sync

trait Logger {

  def trace[F[_]: Sync](msg: => String): F[Unit]

  def debug[F[_]: Sync](msg: => String): F[Unit]

}

object Logger {
  val none = new Logger {
    def trace[F[_]: Sync](msg: => String): F[Unit] = Sync[F].pure(())
    def debug[F[_]: Sync](msg: => String): F[Unit] = Sync[F].pure(())
  }
}
