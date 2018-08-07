package mpc4s.protocol.internal

import minitest._

object CaseSpec extends SimpleTestSuite {

  test("lowerFirst") {
    assertEquals(Case.lowerFirst("Name"), "name")
    assertEquals(Case.lowerFirst("NAME"), "nAME")
    assertEquals(Case.lowerFirst(""), "")
  }

  test("upperFirst") {
    assertEquals(Case.upperFirst("name"), "Name")
    assertEquals(Case.upperFirst("NAME"), "NAME")
    assertEquals(Case.upperFirst(""), "")
  }

  // test("toSnakeCase") {
  //   assertEquals(Case.toSnakeCase("Last-Modified"), "last_modified")
  //   assertEquals(Case.toSnakeCase("dbUpdate"), "db_update")
  //   assertEquals(Case.toSnakeCase("play_time"), "play_time")
  // }
}
