package mpc4s.http.config

import java.nio.file.Path

import mpc4s.http.util.Files._

case class FilenameConfig(basenames: List[String]
  , extensions: List[String]
) {

   lazy val files = for {
    name <- basenames
    ext <- extensions
  } yield s"${name}.${ext}"

}

object FilenameConfig {

  def findFile(f: Path, dirCfg: DirectoryConfig, fileCfg: FilenameConfig): Option[Path] = {
    def fromDir(dir: Path): Option[Path] =
      if (!dir.isDirectory) None
      else dir.findAnyFile(fileCfg.files).
        orElse(dir.findAnyFileInSubDirs(dirCfg.discs, fileCfg.files)).
        orElse {
          if (dirCfg.discs.contains(dir.name)) dir.parent.flatMap(_.findAnyFile(fileCfg.files))
          else None
        }

    if (f.isDirectory) fromDir(f)
    else f.parent.flatMap(fromDir)
  }

}
