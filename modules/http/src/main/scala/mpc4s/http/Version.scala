package mpc4s.http

import mpc4s.protocol._

object Version {
  def longVersion: String = {
    val v =
      BuildInfo.version +
      BuildInfo.gitDescribedVersion.map(c => s" ($c)").getOrElse("")

    if (BuildInfo.gitUncommittedChanges) v + " [dirty workingdir]" else v
  }

  def shortVersion = BuildInfo.version

  def projectString: String = {
    s"mpc4s-http ${longVersion}"
  }
}
