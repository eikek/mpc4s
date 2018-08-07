package mpc4s.player

import fs2.text
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._, io.circe.parser._
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

import mpc4s.http.util.Files._
import mpc4s.player.webjar.Webjars

object Toc {

  def readToc[F[_]](implicit F: Sync[F]): F[Webjars.Toc] = memoize {
    def parseToc(json: String): Webjars.Toc =
      decode[Webjars.Toc](json) match {
        case Right(toc) => toc
        case Left(ex) => throw ex
      }

    val tocUrl = Webjars.getClass.getResource("toc.json")
    tocUrl.open(32 * 1024).
      through(text.utf8Decode).
      fold1(_ + _).
      map(parseToc).
      compile.last.
      map(_.get)
  }

  def find(name: String, path: Seq[String]): Option[(Webjars.ModuleId, URL)] =
    for {
      wj <- Webjars.modules.find(_.artifactId equalsIgnoreCase name)
      url <- makeLocalUrl(wj, path.mkString("/"))
    } yield (wj, url)

  private def makeLocalUrl(wj: Webjars.ModuleId, path: String): Option[URL] = {
    val resource = s"${wj.resourcePrefix}/$path"
    Option(Webjars.getClass.getResource(resource))
  }

  private def memoize[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] = {
    val ref = new AtomicReference[Option[A]](None)
    val mfa = F.delay {
      ref.get match {
        case Some(a) => F.pure(a)
        case None => fa.map(a => {
          ref.compareAndSet(None, Some(a))
          a
        })
      }
    }
    mfa.flatMap(identity)
  }
}
