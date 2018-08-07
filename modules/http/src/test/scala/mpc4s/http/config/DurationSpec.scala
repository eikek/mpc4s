package mpc4s.http.config

import minitest._
import cats.data.Validated
import cats.data.Validated.{invalid, valid}

import Duration.{DurationParser => parser, _}

object DurationSpec extends SimpleTestSuite {

  test("create values") {
    assertEquals(1.hours.minutes, 60)
    assertEquals(2.hours.minutes, 120)
    assertEquals(1.hours.days, 0)
    assertEquals(1.hours.seconds, 3600)
    assertEquals(1.days.hours, 24)
    assertEquals(2.days.hours, 48)
    assertEquals(10.days.hours, 240)
    assertEquals(100.days.hours, 2400)
    assertEquals(200.days.hours, 4800)
    assertEquals(500.days.minutes, 720000)

    assertEquals(1.5.days.hours, 36)
    assertEquals(200.5.days.hours, 4812)
    assertEquals(0.days, 0.minutes)
    assertEquals(0.seconds, 0.minutes)
    assertEquals(0.hours, Duration.zero)

    assertEquals((-2).minutes.seconds, -120)

    assertEquals(Duration.seconds(1.5), Duration.millis(1500))
  }

  test("calculate") {
    assertEquals(96.minutes * 2, 192.minutes)
    assertEquals(10.minutes + 5.minutes, 15.minutes)
    assertEquals(5.minutes - 10.minutes, -5.minutes)
    assertEquals(-4.hours.isNegative, true)
  }

  test("parse hh:mm:ss format") {
    assertEquals(parser.hhmmss.parse("1:36:00"), valid(96.minutes))
    assertEquals(parser.hhmmss.parse("1:10"), valid(1.minutes + 10.seconds))
    assertEquals(parser.hhmmss.parse("1:10:10"), valid(1.hours + 10.minutes + 10.seconds))
    assertEquals(parser.hhmmss.parse("1:1:1"), valid(1.hours + 1.minutes + 1.seconds))
    assertEquals(parser.hhmmss.parse("100:1"), valid(100.minutes + 1.seconds))
    assertEquals(parser.hhmmss.parse("2:1"), valid(2.minutes + 1.seconds))
    assertEquals(parser.hhmmss.parse("-2:1"), valid((2.minutes + 1.seconds) * -1))

    assertEquals(parser.hhmmss.parse("1:100:1"), invalid("Cannot read '1:100:1' near position 5: Minutes must be between 0 and 59"))
    assertEquals(parser.hhmmss.parse("1:1x:2"), invalid("Cannot read '1:1x:2' near position 3: Expected end of string, but got: x:2"))
    assertEquals(parser.hhmmss.parse("1:1x"), invalid("Cannot read '1:1x' near position 3: Expected end of string, but got: x"))
    assertEquals(parser.hhmmss.parse(":10"), invalid("Cannot read ':10' near position 0: Expected matching ^\\d+, but got: :10"))
  }

  test("parse the verbose format") {
    assertEquals(parser.wordy.parse("12 days 20hours 14min 10secs"), valid(12.days + 20.hours + 14.minutes + 10.seconds))
    assertEquals(parser.wordy.parse("20hours 14min 12 days 10secs"), valid(12.days + 20.hours + 14.minutes + 10.seconds))
    assertEquals(parser.wordy.parse("1 day 20hours"), valid(1.days + 20.hours))
    assertEquals(parser.wordy.parse(454841.seconds.format(DurationFormat.wordy)), valid(454841.seconds))

    assertEquals(parser.wordy.parse("1x days"), invalid("Cannot read '1x days' near position 1: Expected one of: d, h, m, s, but got: x days"))
    assertEquals(parser.wordy.parse("3days HAHAHA 6day 1d"), Validated.invalid("Cannot read '3days HAHAHA 6day 1d' near position 6: Expected end of string, but got: HAHAHA 6day 1d"))
    assertEquals(parser.wordy.parse("3days 6day 1d"), Validated.valid(10.days))
  }

  test("not backtrack parsing on some inputs") {
    assertEquals(Duration.parse("1:mx"), invalid("Cannot read '1:mx' near position 2: Expected matching ^\\d+, but got: mx"))
    assertEquals(Duration.parse("1 day X"), invalid("Cannot read '1 day X' near position 6: Expected end of string, but got: X"))
  }

  test("format a duration to hh:mm[:ss]") {
    assertEquals(96.minutes.format(DurationFormat.hhmmss), "1:36:00")
    assertEquals(150.millis.format(DurationFormat.hhmmss), "150ms")
    assertEquals(1.seconds.format(DurationFormat.hhmmss), "00:01")
    assertEquals(63.seconds.format(DurationFormat.hhmmss), "01:03")
    assertEquals(150.millis.formatExact, "PT0.15S")
    assertEquals((42.days + 10.minutes).format(DurationFormat.hhmmss), "1008:10:00")
  }

  test("format a duration to wordy format") {
    assertEquals(96.minutes.format(DurationFormat.wordy), "1 hour 36 min")
    assertEquals(DurationFormat.wordy.format(96.minutes), "1 hour 36 min")

    assertEquals(454841.seconds.format(DurationFormat.wordy), "5 days 6 hours 20 min 41 secs")
    assertEquals(433241.seconds.format(DurationFormat.wordy), "5 days 20 min 41 secs")
    assertEquals(12.seconds.format(DurationFormat.wordy), "12 secs")

    assertEquals((42.days + 10.minutes).format(DurationFormat.wordy), "42 days 10 min")
    assertEquals(120.millis.format(DurationFormat.wordy), "120ms")
  }
}
