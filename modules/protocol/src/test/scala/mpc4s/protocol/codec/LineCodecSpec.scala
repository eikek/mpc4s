package mpc4s.protocol.codec

import shapeless._
import minitest._

import implicits._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.codecs.ChoiceLineCodec
import mpc4s.protocol.ListMap

object LineCodecSpec extends SimpleTestSuite {

  test("empty") {
    assertEquals(cs.empty.write(()), Result.successful(""))
    assertEquals(cs.empty.parse(""), Result.successful(ParseResult((), "")))
    assertEquals(cs.empty.parse("abc").isLeft, true)
  }

  test("ignore") {
    assertEquals(cs.ignore.write(()), Result.successful(""))
    assertEquals(cs.ignore.parse(""), Result.successful(ParseResult((), "")))
    assertEquals(cs.ignore.parse("abc"), Result.successful(ParseResult((), "abc")))

    val c = (cs.ignore :: cs.charsIn("abcde")).dropUnits.head
    assertEquals(c.parse("ababab"), Result.successful(ParseResult("ababab", "")))
  }

  test("constant") {
    val c = cs.constant("abc", ())

    assertEquals(c.parse("abcdefg"), Result.successful(ParseResult((), "defg")))
    assertEquals(c.write(()), Result.successful("abc"))
    assertEquals(c.parse("xyz").isLeft, true)
  }

  test("whitespace") {
    val c = cs.whitespace

    assertEquals(c.parse("    x"), Result.successful(ParseResult((), "x")))
    assertEquals(c.parse("\t  \t x"), Result.successful(ParseResult((), "x")))
    assertEquals(c.parse("  a  x"), Result.successful(ParseResult((), "a  x")))
    assertEquals(c.write(()), Result.successful(" "))
    assertEquals(c.parse("x").isLeft, true)
  }

  test("repeat") {
    val c = cs.repeat(cs.constant("123", ()))

    assertEquals(c.parse("12312312312312"), Result.successful(ParseResult(Vector((),(),(),()), "12")))
    assertEquals(c.parse("abc"), Result.successful(ParseResult(Vector.empty, "abc")))
    assertEquals(c.parse(""), Result.successful(ParseResult(Vector.empty, "")))

    val c2 = cs.repeat(cs.choice(cs.constant("One", 1), cs.constant("Two", 2)))
    assertEquals(c2.write(Vector(1,2,1)), Result.successful("OneTwoOne"))
    assertEquals(c2.parse("TwoTwoOne"), Result.successful(ParseResult(Vector(2,2,1), "")))
  }

  test("repeat minimum") {
    val c = cs.repeat(cs.constant("123", ()), 1)

    assertEquals(c.parse("12").isLeft, true)
  }

  test("repsep") {
    val c = cs.repsep(cs.int, cs.whitespace)
    assertEquals(c.parse("12 13 14 15"), Result.successful(ParseResult(Vector(12, 13, 14, 15), "")))
    assertEquals(c.write(Vector(1,2,3)), Result.successful("1 2 3"))

    val c2 = cs.repsep(cs.int, cs.constant("-", ()))
    assertEquals(c2.parse("1-2-3-5-8"), Result.successful(ParseResult(Vector(1, 2, 3, 5, 8), "")))
    assertEquals(c2.write(Vector(1,2,3)), Result.successful("1-2-3"))
  }

  test("option") {
    val c = cs.option(cs.constant("123", ()))

    assertEquals(c.parse("123"), Result.successful(ParseResult(Some(()), "")))
    assertEquals(c.parse("12"), Result.successful(ParseResult(None, "12")))
    assertEquals(c.write(None), Result.successful(""))
    assertEquals(c.write(Some(())), Result.successful("123"))
  }

  test("combine") {
    val c = cs.constant("abc", 1) :: cs.constant("123", 2)

    assertEquals(c.parse("abc123"), Result.successful(ParseResult(1 :: 2 :: HNil, "")))
  }

  test("rest") {
    val c = cs.constant("123", 123) :: cs.rest

    assertEquals(c.parse("123abcde"), Result.successful(ParseResult(123 :: "abcde" :: HNil, "")))
    assertEquals(c.write(123 :: "blabla" :: HNil), Result.successful("123blabla"))
  }

  test("filter") {
    val c = cs.filter(_ == 'a')

    assertEquals(c.parse("aaabb"), Result.successful(ParseResult("aaa", "bb")))
    assertEquals(c.write("aaa"), Result.successful("aaa"))
    assertEquals(c.write("abc").isLeft, true)
  }

  test("chars in") {
    val c = cs.charsIn('a' to 'z')

    assertEquals(c.parse("abcdei 123"), Result.successful(ParseResult("abcdei", " 123")))
    assertEquals(c.write("abcdz"), Result.successful("abcdz"))
    assertEquals(c.write("abc123").isLeft, true)
  }

  test("chars not in") {
    val c = cs.charsNotIn(")")

    assertEquals(c.parse("abc)de"), Result.successful(ParseResult("abc", ")de")))
    assertEquals(c.write("ab123 12"), Result.successful("ab123 12"))
    assertEquals(c.write("(+ 1 2)").isLeft, true)
    assertEquals(c.parse(")"), Result.successful(ParseResult("", ")")))
  }

  test("regex") {
    val c = cs.regex("""ls [-\+]{1,2}[a-z]""".r)

    assertEquals(c.parse("ls --a"), Result.successful(ParseResult("ls --a", "")))
    assertEquals(c.parse("ls --acde"), Result.successful(ParseResult("ls --a", "cde")))
    assertEquals(c.write("ls --z"), Result.successful("ls --z"))
    assertEquals(c.write("ls +bbbb").isLeft, true)
  }

  test("integer") {
    val c = cs.bigint

    assertEquals(c.parse("121abc"), Result.successful(ParseResult(BigInt(121), "abc")))
    assertEquals(c.parse("121\n23"), Result.successful(ParseResult(BigInt(121), "\n23")))
    assertEquals(c.write(BigInt(42)), Result.successful("42"))
  }

  test("decimal") {
    val c = cs.decimal

    assertEquals(c.parse("1.2121"), Result.successful(ParseResult(BigDecimal(1.2121), "")))
    assertEquals(c.parse("-5e-10A"), Result.successful(ParseResult(BigDecimal("-5e-10"), "A")))
    assertEquals(c.parse("0.000000"), Result.successful(ParseResult(BigDecimal(0.0), "")))
    assertEquals(c.write(BigDecimal(1.02)), Result.successful("1.02"))
  }

  test("atEnd") {
    val c = (cs.regex("[a-z]+".r) :: cs.empty).dropUnits.head

    assertEquals(c.parse("abc"), Result.successful(ParseResult("abc", "")))
    assertEquals(c.parse("abc ").isLeft, true)
    assertEquals(c.parse("abcde123").isLeft, true)
    assertEquals(c.write("abcde"), Result.successful("abcde"))
    assertEquals(c.write("abc123").isLeft, true)
  }

  test("choice") {
    val c = cs.constant("a", 1).orElse(cs.constant("b", 2))

    assertEquals(c.parse("a"), Result.successful(ParseResult(1, "")))
    assertEquals(c.parse("b"), Result.successful(ParseResult(2, "")))
    assertEquals(c.parse("c").isLeft, true)
    assertEquals(c.write(1), Result.successful("a"))
    assertEquals(c.write(2), Result.successful("b"))

    c.orElse(cs.constant("c", 3)) match {
      case ac: ChoiceLineCodec[_] =>
        assertEquals(ac.codecs.size, 3)
      case x =>
        fail(s"Unexpected type: $x")
    }

    sealed trait Color
    case object Red extends Color
    case object Green extends Color

    val red = cs.constant[Color]("red", Red)
    val green = cs.constant[Color]("green", Green)
    val color = red.orElse(green)
    assertEquals(color.parse("red"), Result.successful(ParseResult(Red, "")))
    assertEquals(color.parse("green"), Result.successful(ParseResult(Green, "")))
    assertEquals(color.parse("blue").isLeft, true)
    assertEquals(color.write(Red), Result.successful("red"))
    assertEquals(color.write(Green), Result.successful("green"))
  }

  test("head") {
    val c = (cs.int :: cs.constant("\n", ())).dropUnits.head.repeat
    assertEquals(c.parse("12\n13\nabc"), Result.successful(ParseResult(Vector(12, 13), "abc")))
    assertEquals(c.write(Vector(8,9)), Result.successful("8\n9\n"))
  }

  test("keyValue") {
    val c = cs.keyValue

    assertEquals(c.parse("a: 1\nb:2\nc: 3\n"), Result.successful(ParseResult(ListMap.strings("a" -> "1", "b" -> "2", "c" -> "3"), "")))
    assertEquals(c.write(ListMap.strings("name" -> "screw", "count" -> "100")), Result.successful("name: screw\ncount: 100\n"))
    assertEquals(c.write(ListMap.strings("count" -> "100", "name" -> "screw")), Result.successful("count: 100\nname: screw\n"))
  }

  test("key-value case class") {
    // this import defines an implicit LineCodec[String] !
    import keyvalues._

    case class Config(path: String, timeout: Int, enabled: Boolean, key: String)

    val c = LineCodec[Config].keyValues

    val cfg = Config("/root/a", 121, true, "key12")
    val str = "path: /root/a\ntimeout: 121\nenabled: 1\nkey: key12\n"
    assertEquals(c.parse(str), Result.successful(ParseResult(cfg, "")))
    assertEquals(c.write(cfg), Result.successful(str))
  }

  test("fallback") {
    val c = cs.fallback(cs.int, cs.regex("[abc]+".r))

    assertEquals(c.write(Left(12)), Result.successful("12"))
    assertEquals(c.write(Right("abcba")), Result.successful("abcba"))
    assertEquals(c.write(Right("abcbaz")).isLeft, true)
    assertEquals(c.parse("15"), Result.successful(ParseResult(Left(15), "")))
    assertEquals(c.parse("abcba"), Result.successful(ParseResult(Right("abcba"), "")))
  }

  test("split") {
    val c = cs.split("file:".r)

    assertEquals(c.parse("file: afile: bfile: c"),
      Result.successful(ParseResult(Vector("file: a", "file: b", "file: c"), "")))
    assertEquals(c.parse("file: af"),
      Result.successful(ParseResult(Vector("file: af"), "")))
    assertEquals(c.parse("abcfile: x").isLeft, true)

    assertEquals(c.write(Vector("a","b")).isLeft, true)
    assertEquals(c.write(Vector("file: a", "file: b")), Result.successful("file: afile: b"))
  }

  test("splitSongs") {
    val c = cs.splitSongs

    assertEquals(c.parse("file: blabla\nfile: uberfile: blupblup\nfile: blipblip\n"),
      Result.successful(ParseResult(Vector("file: blabla\n", "file: uberfile: blupblup\n", "file: blipblip\n"), "")))

  }

  test("map") {
    val c = cs.map(cs.split("1".r), cs.int)

    assertEquals(c.parse("123132"), Result.successful(ParseResult(Vector(123, 132), "")))
    assertEquals(c.write(Vector(122, 133)), Result.successful("122133"))
  }

  test("quotedString") {
    val c = cs.quotedString

    assertEquals(c.write("""whitespace "and" quotes"""), Result.successful(""""whitespace \"and\" quotes""""))
    assertEquals(c.parse(""""whitespace \"and\" quotes""""), Result.successful(ParseResult("""whitespace "and" quotes""", "")))
    assertEquals(c.parse("\"tell me\" nothing"), Result.successful(ParseResult("tell me", " nothing")))
  }

  test("derive from case class (hlist)") {
    implicit def string = cs.charsIn(('a' to 'z') ++ ('A' to 'Z') ++ "-_ ")

    case class Group(name: String, members: Int)

    val c = LineCodec[Group].derive

    assertEquals(c.parse("this is the name 154"), Result.successful(ParseResult(Group("this is the name ", 154), "")))
    assertEquals(c.write(Group("my name", 12)), Result.successful("my name12"))
  }

  test("derive from hlist") {
    case class Range(from: Int, to: Int)

    val c = (cs.constant("range", ()) ::
      cs.whitespace ::
      cs.int ::
      cs.constant(":", ()) ::
      cs.int).dropUnits.as[Range]

    assertEquals(c.parse("range 1:12"), Result.successful(ParseResult(Range(1, 12), "")))
    assertEquals(c.write(Range(12, 25)), Result.successful("range 12:25"))
  }

  test("derive from coproduct") {
    val c = LineCodec[Animal].choice

    assertEquals(c.write(Animal.Cat(2)), Result.successful("cat 2"))
    assertEquals(c.write(Animal.Dog("bello")), Result.successful("dog bello"))
    assertEquals(c.parse("tiger 1X"), Result.successful(ParseResult(Animal.Tiger(true), "X")))
    assertEquals(c.parse("cat 15X"), Result.successful(ParseResult(Animal.Cat(15), "X")))
    assertEquals(c.parse("dog bello X"), Result.successful(ParseResult(Animal.Dog("bello"), " X")))
    assertEquals(c.parse("xyz").isLeft, true)
  }
}
