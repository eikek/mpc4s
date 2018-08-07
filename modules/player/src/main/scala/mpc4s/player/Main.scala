package mpc4s.player

import cats.effect.IO
import mpc4s.http.{Main => HttpServer}

object Main {

  def main(args: Array[String]): Unit = {
    new HttpServer(cfg => Some(Endpoint[IO](cfg))).run(args)
  }

}
