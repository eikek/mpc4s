package mpc4s.http.config

import java.nio.file.Path
import mpc4s.http.util.Files._

case class MpdConfigs(configs: Map[String, MpdConfig]) {

  val default: MpdConfig =
    configs.find(_._1 == "default").
      getOrElse(sys.error("Configuration problem: no mpd connection with id 'default' found"))._2

  def map[B](f: (String, MpdConfig) => B): List[B] =
    configs.map(f.tupled).toList

  def nonEmptyPassword: MpdConfigs =
    MpdConfigs(configs.map({case (k, c) => (k, c.nonEmptyPassword)}))

  def setMusicDirectory(md: Path): MpdConfigs =
    MpdConfigs(configs.map({case (k, c) => c.musicDirectory match {
      case Some(path) =>
        if (!path.exists) sys.error(s"The music directory '$path' for mpd '$k' doesn't exist")
        else (k, c)
      case None => (k, c.copy(musicDirectory = Some(md)))
    }}))
}
