package mpc4s.client

import minitest._
import fs2._
import cats.effect.IO

object ResponseSplitSpec extends SimpleTestSuite {

  val splitter: Pipe[IO,Byte,String] = ResponseSplit.responsesUtf8[IO]()

  val ack = "ACK [50@2] {play} file not found\n"
  val ok = "file: abc\npos: 121\nid: 22\nOK\n"

  test("single response") {
    val r0 = run(makeStream(ack))
    assertEquals(r0, Vector(ack))

    val r1 = run(makeStream(ok))
    assertEquals(r1, Vector(ok))
  }

  test("multiple responses") {
    val r0 = run(makeStream(ok + ok))
    assertEquals(r0, Vector(ok, ok))

    val r1 = run(makeStream(ok + ack))
    assertEquals(r1, Vector(ok, ack))

    val r2 = run(makeStream(ack + ok))
    assertEquals(r2, Vector(ack, ok))
  }

  test("unfinished") {
    val r0 = run(makeStream(ok + ok.substring(0, 12)))
    assertEquals(r0, Vector(ok, ok.substring(0, 12)))
  }

  test("multiple chunks") {
    val r0 = run(makeStream(ok + ok + ok, 30))
    assertEquals(r0, Vector(ok, ok, ok))
  }

  test("arrayconcat") {
    val a0 = array(1, 2, 3, 4)
    val a1 = array(50,-12)
    assertEquals(ResponseSplit.concat(a0, a1).toList, List[Byte](1,2,3,4,50,-12))
  }

  test("small file") {
    val r0 = run(loadResource("/album.txt"))
    val head = loadResource("/album.txt").
      take(200).
      through(text.utf8Decode).
      fold1(_ + _).
      compile.last.unsafeRunSync.get

    assertEquals(r0.size, 1)
    assertEquals(r0.head.take(head.size), head)

    val all = loadResource("/album.txt").
      through(text.utf8Decode).
      fold1(_ + _).
      compile.last.unsafeRunSync.get

    assertEquals(r0.head, all)
  }

  test("big file") {
    val r0 = run(loadResource("/songlist.txt"))
    val head = loadResource("/songlist.txt").
      take(200).
      through(text.utf8Decode).
      fold1(_ + _).
      compile.last.unsafeRunSync.get

    assertEquals(r0.size, 1)
    assertEquals(r0.head.take(head.size), head)

    val all = loadResource("/songlist.txt").
      through(text.utf8Decode).
      fold1(_ + _).
      compile.last.unsafeRunSync.get

    assertEquals(r0.head, all)
  }

  test("big + small file") {
    val r0 = run(loadResource("/songlist.txt") ++ loadResource("/album.txt"))
    assertEquals(r0.size, 2)

    val r1 = run(loadResource("/album.txt") ++ loadResource("/songlist.txt"))
    assertEquals(r1.size, 2)
  }

  def run(in: Stream[IO,Byte]): Vector[String] =
    in.through(splitter).compile.toVector.unsafeRunSync

  def makeStream(cnt: String, chunkSize: Int = 32 * 1024): Stream[IO, Byte] =
    Stream(cnt).
      flatMap(s => Stream.chunk(Chunk.bytes(s.getBytes))).
      segmentN(chunkSize, true).
      flatMap(c => Stream.segment(c)).
      covary[IO]

  def loadResource(name: String, chunkSize: Int = 32 * 1024): Stream[IO, Byte] = {
    val in = getClass.getResourceAsStream(name)
    if (in == null) sys.error("no resoure: "+ name)

    fs2.io.readInputStream(IO(in), chunkSize, true)
  }

  def array(bs: Byte*): Array[Byte] =
    bs.toArray


}
