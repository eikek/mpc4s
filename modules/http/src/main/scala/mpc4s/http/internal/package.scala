package mpc4s.http

import java.nio.file.Path

package object internal {

  type PathCache[F[_]] = Cache[F, String, Option[Path]]

}
