package mpc4s.http.config

import java.nio.file.Path
import pureconfig._

case class AppConfig(
  appName: String
    , baseurl: String
    , musicDirectory: Path
    , mpd: MpdConfigs
    , cover: CoverConfig
    , customContent: CustomContentConfig
)

object AppConfig {

  val default: AppConfig = {
    val c = loadConfig[AppConfig]("mpc4s.http").get
    c.copy(mpd = c.mpd.nonEmptyPassword.setMusicDirectory(c.musicDirectory))
  }

}
