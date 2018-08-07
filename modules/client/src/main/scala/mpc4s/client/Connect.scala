package mpc4s.client

import java.net.{InetAddress, InetSocketAddress}

case class Connect(address: InetSocketAddress, credentials: Option[Password] = None) {

  def withPassword(cred: Password): Connect =
    copy(credentials = Some(cred))

  def withPassword(pass: Option[Password]): Connect =
    copy(credentials = pass)

  def withoutPassword: Connect =
    credentials.map(_ => copy(credentials = None)).getOrElse(this)
}

object Connect {

  def apply(host: String, port: Int): Connect =
    Connect(new InetSocketAddress(host, port))

  def apply(address: InetAddress, port: Int): Connect =
    Connect(new InetSocketAddress(address, port))

  def byIp(address: String, port: Int): Connect =
    Connect(
      InetAddress.getByAddress(address.split('.').toList.
        map(_.toInt.toByte).
        toArray),
      port)
}
