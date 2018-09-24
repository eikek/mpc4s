package mpc4s.http

import java.nio.file.{Path, Paths}
import pureconfig._
import pureconfig.error._
import pureconfig.ConvertHelpers._

import mpc4s.http.util.Size

package object config {

  implicit final class ConfigEitherOps[A](r: Either[ConfigReaderFailures, A]) {
    def get: A = r match {
      case Right(a) => a
      case Left(errs) => sys.error(errs.toString)
    }
  }

  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, KebabCase))

  implicit val pathConvert: ConfigReader[Path] = ConfigReader.fromString[Path](catchReadError(s =>
    if (s.isEmpty) throw new Exception("Empty path is not allowed: "+ s)
    else Paths.get(s)
  ))

  implicit val durationConvert: ConfigReader[Duration] = ConfigReader.fromString[Duration](catchReadError(s =>
    Duration.unsafeParse(s)
  ))

  implicit val sizeConvert: ConfigReader[Size] = ConfigReader.fromString[Size](catchReadError(s =>
    Size.parse(s) match {
      case None => throw new Exception(s"Invalid size: $s. Use units G,M,K or B")
      case Some(sz) => sz
    }
  ))
}
