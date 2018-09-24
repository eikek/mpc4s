package mpc4s.http.util

import java.net.URL

case class ResourceFile(url: URL
  , name: String
  , checksum: String
  , length: Size
  , mime: String
)

object ResourceFile {

  val pictureUnsplash = {
    val url = getClass.getResource("/jamison-mcandie-112375-unsplash.jpg")
    ResourceFile(url
      , "jamison-mcandie-112375-unsplash.jpg"
      , "0b31b7655b2a55cd90abf8747b872db1ba2886073dede8b356da9b8f4de378d7"
      , Size.Bytes(56666L)
      , "image/jpg")
  }
}
