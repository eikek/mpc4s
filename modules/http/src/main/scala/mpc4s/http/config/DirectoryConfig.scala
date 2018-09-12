package mpc4s.http.config

case class DirectoryConfig(discDirectories: List[String]
  , discSeparators: List[String]
  , discNumbers: List[String]
  , cacheSize: Int
) {

  lazy val discs = for {
    name <- discDirectories
    sep <- discSeparators
    num <- discNumbers
  } yield s"${name}${sep}${num}"

}
