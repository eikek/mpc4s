package mpc4s.http

import io.circe._, io.circe.generic.semiauto._

case class JsonCommand(command: String)

object JsonCommand {

  implicit def decoder: Decoder[JsonCommand] = deriveDecoder[JsonCommand]

}
