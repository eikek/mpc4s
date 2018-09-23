package mpc4s.http.config

import java.nio.file.Path
import mpc4s.http.util.Size

case class ThumbnailConfig(
  enable: Boolean
    , minFileSize: Size
    , maxFileSize: Size
    , directory: Path
    , maxParallel: Int
) {

  require(minFileSize < maxFileSize
    , s"min file size ($minFileSize) must be lower than max file size ($maxFileSize)")
}
