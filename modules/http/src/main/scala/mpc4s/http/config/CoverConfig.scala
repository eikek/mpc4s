package mpc4s.http.config

case class CoverConfig(basenames: List[String]
  , extensions: List[String]
  , discDirectories: List[String]
  , discSeparators: List[String]
  , discNumbers: List[String]
  , cacheSize: Int
) {

  lazy val files = for {
    name <- basenames
    ext <- extensions
  } yield s"${name}.${ext}"

  lazy val discs = for {
    name <- discDirectories
    sep <- discSeparators
    num <- discNumbers
  } yield s"${name}${sep}${num}"
}
