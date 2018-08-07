package mpc4s.protocol

import minitest._

object CommandNameSpec extends SimpleTestSuite {

  test("playing") {

    // val test = CommandName.makeTree(List(
    //   CommandName("find")
    //     , CommandName("close")
    //     , CommandName("sticker", "get")
    //     , CommandName("sticker", "set")
    //     , CommandName("sticker", "all")
    //     , CommandName("kill")))


    // println(test)

    // val cmd = CommandName.find("sticker get all", test)
    // println(cmd)

    println(CommandName("sticker", "get"))
  }
}
