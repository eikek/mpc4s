package mpc4s.http.util

import java.nio.file.Path
import fs2.Stream
import cats.effect.Effect
import spinoco.protocol.http.header.{Range => _, _}
import spinoco.protocol.http.header.value._
import spinoco.protocol.http.{Uri => HttpUri}
import spinoco.fs2.http.routing._
import org.log4s.{Error => _, _}

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.http._
import mpc4s.http.config.CoverConfig
import JsonBody._
import Responses._
import Files._

trait Matchers {

  private[this] val logger = getLogger

  /** Stop backtracking in the `choice` combinator. */
  def cut[F[_]](m: Route[F]): Route[F] =
    m.recover(resp => Matcher.success(Stream.emit(resp)))

  def ifNoneMatch[F[_]]: Matcher[F, Option[String]] =
    header[`If-None-Match`].? map {
      case Some(`If-None-Match`(EntityTagRange.Range(List(EntityTag(tag, false))))) => Some(tag)
      case _ => None
    }

  def restPath: Matcher[Nothing, Seq[String]] =
    path.map(p => p.segments)

  def mpdCommand[F[_]: Effect](codec: LineCodec[Command]): Matcher[F, Command] =
    jsonBody[F, JsonCommand].flatMap { cmd =>
      if (cmd.command.isEmpty) Matcher.respond(BadRequest.body(Error("Command required")))
      else codec.parseValue(cmd.command) match {
        case Right(c) => Matcher.success(c)
        case Left(err) => Matcher.respond(BadRequest.body(Error(s"Error parsing command '${cmd.command}': ${err.message}")))
      }
    }

  def filter[F[_]]: Matcher[F, Filter] =
    Matcher.Match[F, Filter] { (header, _) =>
      val list = header.query.collect(Function.unlift {
        case HttpUri.QueryParameter.Single(name, v) =>
          Some((name, v))
        case _ => None
      })

      val filter = Filter.fromTuples(list)
      if (filter.isEmpty) MatchResult.Failed[F](BadRequest.body(Error("Some filter must be specified for search")))
      else MatchResult.success(filter)
    }

  def listType[F[_]]: Matcher[F, ListType] =
    param[ListType]("listtype")

  def sort: Matcher[Nothing, Sort] =
    param[Sort]("sort")

  def range: Matcher[Nothing, Range] =
    param[Range]("range")

  def asFile(root: Path): Matcher[Nothing, Path] =
    path.flatMap { p =>
      val file = p.segments.foldLeft(root)(_ / _)
      // if file.parent < root -> error
      if (file.isSubpathOf(root)) {
        logger.trace(s"Find cover for input file '$file'")
        Matcher.success(file)
      } else {
        logger.info(s"Try getting cover for file outside root ('$root'): '$file'")
        Matcher.respond(NotFound.emptyBody)
      }
    }

  def coverFile(root: Path, cfg: CoverConfig): Matcher[Nothing, Path] =
    path.flatMap { p =>
      val file = p.segments.foldLeft(root)(_ / _)
      // if file.parent < root -> error
      if (file.isSubpathOf(root)) {
        logger.trace(s"Find cover for input file '$file'")
        findCoverFile(file, cfg) match {
          case Some(cf) => Matcher.success(cf)
          case None => Matcher.respond(NotFound.emptyBody)
        }
      } else {
        logger.info(s"Try getting cover for file outside root ('$root'): '$file'")
        Matcher.respond(NotFound.emptyBody)
      }
    }

  def findCoverFile(f: Path, cfg: CoverConfig): Option[Path] = {
    def fromDir(dir: Path): Option[Path] =
      if (!dir.isDirectory) None
      else dir.findAnyFile(cfg.files).
        orElse(dir.findAnyFileInSubDirs(cfg.discs, cfg.files)).
        orElse {
          if (cfg.discs.contains(dir.name)) dir.parent.flatMap(_.findAnyFile(cfg.files))
          else None
        }

    if (f.isDirectory) fromDir(f)
    else f.parent.flatMap(fromDir)
  }


  implicit def lineCodecStringDecoder[A](implicit lc: LineCodec[A]): StringDecoder[A] =
    StringDecoder(str => lc.parseValue(str).toOption)
}

object Matchers extends Matchers
