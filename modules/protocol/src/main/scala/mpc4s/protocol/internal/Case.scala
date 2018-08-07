package mpc4s.protocol.internal

object Case {

  // from pureconfig: https://github.com/pureconfig/pureconfig/blob/635473404515df35d55f777182d3af7567f2914f/core/src/main/scala/pureconfig/NamingConvention.scala#L14
  private val wordBreakPattern = String.format(
    "%s|%s|%s",
    "(?<=[A-Z])(?=[A-Z][a-z])",
    "(?<=[^A-Z])(?=[A-Z])",
    "(?<=[A-Za-z])(?=[^A-Za-z])").r

  /** Convert camelCase to snake_case */
  def camelToSnake(key: String): String =
    wordBreakPattern.split(key).map(_.toLowerCase).mkString("_")


  def lowerFirst(s: String): String =
    if (s == null || s.isEmpty) s
    else s.charAt(0) match {
      case c if c >= 'a' => s
      case c => (c + 32).toChar + s.substring(1)
    }

  def upperFirst(s: String): String =
    if (s == null || s.isEmpty) s
    else s.charAt(0) match {
      case c if c <= 'Z' => s
      case c => (c - 32).toChar + s.substring(1)
    }

  /** Try to convert “any-case” string to snake case.
    */
  // def toSnakeCase(s: String): String = {
  //   val cc = s.split("[\\-_]").map(upperFirst).mkString
  //   camelToSnake(lowerFirst(cc))
  // }


  def startsWithIgnoreCase(str: String, input: String): Boolean = {
    val inLen = input.length
    val strLen = str.length
    if (inLen < strLen) false
    else {
      val sub = input.substring(0, strLen)
      sub.equalsIgnoreCase(str)
    }
  }

}
