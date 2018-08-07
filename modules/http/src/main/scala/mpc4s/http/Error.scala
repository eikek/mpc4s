package mpc4s.http

import io.circe._

case class Error(message: String)

object Error {

  implicit def encoder: Encoder[Error] =
    Encoder.instance(e => Json.obj(
      ("success", Json.fromBoolean(false)),
      ("type", Json.fromString("Error")),
      ("error", Json.obj("message" -> Json.fromString(e.message)))
    ))
}
