package mpc4s.http.routes

import fs2.Stream
import spinoco.fs2.http.routing._
import io.circe._, io.circe.generic.semiauto._, io.circe.syntax._

import mpc4s.protocol._
import mpc4s.http.config._
import mpc4s.http.util.all._

object Version {

  def apply[F[_]](conn: MpdConfigs): Route[F] =
    Get map { _ =>
      Stream(Ok.body[F, VersionInfo](VersionInfo(conn))).covary[F]
    }

  case class VersionInfo(mpdConnections: List[VersionInfo.MpdConnection]
    , name: String = "mpc4s"
    , version: String = BuildInfo.version
    , gitCommit: String = BuildInfo.gitHeadCommit.getOrElse("")
    , gitCommitDate: String = BuildInfo.gitHeadCommitDate.getOrElse("")
    , gitUncommittedChanges: Boolean = BuildInfo.gitUncommittedChanges
    , builtAt: String = BuildInfo.builtAtString
    , builtAtMillis: Long = BuildInfo.builtAtMillis
  )

  object VersionInfo {

    def apply(cfgs: MpdConfigs): VersionInfo = {
      val conns = cfgs.configs.map({ case (id, mpdconf) =>
        MpdConnection(id, mpdconf.host, mpdconf.port, mpdconf.title)
      })
      VersionInfo(conns.toList)
    }

    implicit def encoder: Encoder[VersionInfo] =
      deriveEncoder[VersionInfo]

    case class MpdConnection(id: String, host: String, port: Int, title: String)
    object MpdConnection {
      implicit def encoder: Encoder[MpdConnection] =
        deriveEncoder[MpdConnection]
    }
  }

  implicit val mpdEncoder: Encoder[MpdConfig] =
    Encoder(cfg => Json.obj(
      ("host", cfg.host.asJson),
      ("port", cfg.port.asJson)
    ))
}
