package mpc4s.http.routes

import java.nio.file.Path
import cats.effect.Sync
import shapeless.{::, HNil}
import spinoco.fs2.http.routing._

import mpc4s.http.config._
import mpc4s.http.util.all._

object CustomContent {

  def apply[F[_]: Sync](cfg: CustomContentConfig): Route[F] =
    if (cfg.enabled) directory(cfg.directory)
    else Matcher.respond(NotFound.emptyBody[F])

  def directory[F[_]: Sync](dir: Path): Route[F] = choice(
    Get >> empty >> ifNoneMatch map { noneMatch =>
      staticFile(dir.resolve("index.html"), dir, noneMatch)
    },
    Get >> ifNoneMatch :: restPath map {
      case noneMatch :: path :: HNil  =>
        val file = path.foldLeft(dir)(_ / _)
        staticFile(file, dir, noneMatch)
    })

}
