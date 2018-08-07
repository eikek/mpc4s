package mpc4s.http.config

import pureconfig._

case class ServerBind(host: String, port: Int)

object ServerBind {

  val default = loadConfig[ServerBind]("mpc4s.http.bind").get

}
