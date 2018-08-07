package mpc4s.http.config

import java.nio.file.Path
import mpc4s.client._

case class MpdConfig(host: String
  , port: Int
  , password: Option[Password]
  , maxConnections: Int
  , timeout: Duration
  , musicDirectory: Option[Path]
  , title: String) {

  def nonEmptyPassword: MpdConfig =
    if (password.exists(_.isEmpty)) copy(password = None)
    else this
}
