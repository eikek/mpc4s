package mpc4s.client

final class Password(val value: String) extends AnyVal {

  def asArray: Array[Char] = value.toCharArray

  def isEmpty: Boolean = value.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def masked: Password = Password.masked

  override def toString(): String = "***"
}

object Password {

  val masked = new Password("***")

  def apply(pw: String): Password = new Password(pw)

}
