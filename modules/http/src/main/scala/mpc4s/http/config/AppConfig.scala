package mpc4s.http.config

import java.nio.file.Path
import pureconfig._
import spinoco.protocol.http.Uri

case class AppConfig(
  appName: String
    , baseurl: String
    , musicDirectory: Path
    , mpd: MpdConfigs
    , albumFile: DirectoryConfig
    , cover: FilenameConfig
    , booklet: FilenameConfig
    , customContent: CustomContentConfig
) {

  val baseUri: Uri = Uri.parse(baseurl).toEither match {
    case Right(uri) =>
      uri.copy(path = uri.path.copy(initialSlash = true))
    case Left(err) =>
      throw new Exception(s"Invalid baseurl: $err")
  }

}

object AppConfig {

  val default: AppConfig = {
    val c = loadConfig[AppConfig]("mpc4s.http").get
    c.copy(mpd = c.mpd.nonEmptyPassword.setMusicDirectory(c.musicDirectory))
  }

}
