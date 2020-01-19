package mpc4s.http.internal

import cats.effect.Sync
import cats.Applicative
import cats.implicits._
import fs2.async
import org.log4s._

trait Cache[F[_], A,B] {

  def size: F[Int]

  def clear: F[Unit]

  def getOrCreate(key: A, value: F[B]): F[B]

}


object Cache {
  private[this] val logger = getLogger

  def empty[F[_]: Sync, A, B](maxSize: Int): F[Cache[F,A,B]] =
    if (maxSize <= 0) Sync[F].pure(none[F,A,B])
    else async.refOf(Map.empty[A,B]).map { ref =>
      new Cache[F,A,B] {

        def size: F[Int] =
          ref.get.map(_.size)

        def clear: F[Unit] =
          ref.setAsync(Map.empty[A,B])

        def getOrCreate(key: A, value: F[B]): F[B] = {

          def op: F[B] = ref.get.flatMap { m =>
            m.get(key) match {
              case Some(b) =>
                Sync[F].delay(logger.trace(s"Cache hit for key $key")).map(_ => b)
              case None =>
                Sync[F].delay(logger.trace(s"Cache miss for key '$key'")) >>
                value.flatMap { v =>
                  if (m.size >= maxSize) Sync[F].pure(v)
                  else ref.
                    tryUpdate(m => m.updated(key, v)).
                    flatMap {
                      case Some(c) => Sync[F].pure(v)
                      case None => op
                    }
                }
            }
          }
          op
        }
      }
    }

  def none[F[_]: Applicative,A,B] = new Cache[F,A,B] {

    def size: F[Int] = Applicative[F].pure(0)

    def clear: F[Unit] = Applicative[F].pure(())

    def getOrCreate(key: A, value: F[B]): F[B] = value

  }
}
