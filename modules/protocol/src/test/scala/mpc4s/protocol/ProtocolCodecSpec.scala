package mpc4s.protocol

import minitest._
import java.time.Instant
import mpc4s.protocol.codec._
import mpc4s.protocol.commands._
import mpc4s.protocol.answer._

object ProtocolCodecSpec extends SimpleTestSuite {

  val instant = Instant.parse("2018-07-08T13:27:52Z")

  test("tag") {
    val c = Tag.codec

    assertEquals(c.write(Tag.Albumartist), Result.successful("albumartist"))
    assertEquals(c.parseValue("albumartist"), Result.successful(Tag.Albumartist))
  }

  test("ack") {
    val ack = Ack(Ack.Code.FileNotFound, 2, "play", "file not found")
    assertEquals (
      Ack.codec.write(ack),
      Result.successful("ACK [50@2] {play} file not found")
    )
    assertEquals(
      Ack.codec.parse("ACK [50@2] {play} file not found"),
      Result.successful(ParseResult(ack, ""))
    )
  }

  test("changeevent") {
    val c = ChangeEvent.codec

    assertEquals(c.parse("changed: mixer"), Result.successful(ParseResult(ChangeEvent(Subsystem.Mixer), "")))
    assertEquals(c.write(ChangeEvent(Subsystem.Database)), Result.successful("changed: database"))
  }

  test("song") {
    val c = Song.codec

    val s1 = Song(Uri("music/flac/song.flac"), Some(instant), Some(Seconds(424)), Some(424.2), ListMap.empty)
    assertEquals(c.parse(c.write(s1).right.get), Result.successful(ParseResult(s1, "")))
  }

  test("playlistsong") {
    val c = PlaylistSong.codec

    val s1 = Song(Uri("music/flac/song.flac"), Some(instant), Some(Seconds(424)), Some(424.2), ListMap.empty)
    val ps1 = PlaylistSong(s1, 2, Id("99"))
    assertEquals(c.parse(c.write(ps1).right.get), Result.successful(ParseResult(ps1, "")))

    val remoteSong = "file: http://some/url.m3u\nId: 254\nPos: 121\n"
    assertEquals(c.parseValue(remoteSong)
      , Result.successful(PlaylistSong(Song(Uri("http://some/url.m3u"),None,None,None,ListMap.empty), 121, Id("254"))))
  }

  test("count command") {
    val c = Count.codec

    assertEquals(c.parseValue("count genre classical group artist")
      , Result.successful(Count.FilterAndGroup(Filter.tags(Tag.Genre -> "classical"), Tag.Artist)))
    assertEquals(c.parseValue("count genre classical")
      , Result.successful(Count.FilterOnly(Filter.tags(Tag.Genre -> "classical"))))
    assertEquals(c.parseValue("count group artist"), Result.successful(Count.GroupOnly(Tag.Artist)))
    assertEquals(c.write(Count.GroupOnly(Tag.Artist)), Result.successful("count group artist"))
    assertEquals(c.write(Count.FilterOnly(Filter.tags(Tag.Genre -> "classical")))
      , Result.successful("count genre classical"))
    assertEquals(c.write(Count.FilterAndGroup(Filter.tags(Tag.Genre -> "classical"), Tag.Artist))
      , Result.successful("count genre classical group artist"))
  }

  test("song count") {
    val c = SongCount.codec

    assertEquals(c.parseValue("songs: 12\nplaytime: 2121\n"), Result.successful(SongCount(12, Seconds(2121), None)))
    assertEquals(c.parseValue("Artist: test\nsongs: 12\nplaytime: 2222\n"),
      Result.successful(SongCount(12, Seconds(2222), Some(TagVal(Tag.Artist, "test")))))

    assertEquals(c.write(SongCount(12, Seconds(2121), None)), Result.successful("songs: 12\nplaytime: 2121\n"))
    assertEquals(c.write(SongCount(12, Seconds(2222), Some(TagVal(Tag.Artist, "test")))),
      Result.successful("artist: test\nsongs: 12\nplaytime: 2222\n"))
  }

  test("songCountList") {
    val c = SongCountList.codec

    assertEquals(c.parseValue("songs: 12\nplaytime: 1212\n"),
      Result.successful(SongCountList(SongCount(12, Seconds(1212), None))))
    assertEquals(c.parseValue("composer: test1\nsongs: 12\nplaytime: 535\n"),
      Result.successful(SongCountList(SongCount(12, Seconds(535), Some(TagVal(Tag.Composer, "test1"))))))
    assertEquals(c.parseValue("Composer: test1\nsongs: 12\nplaytime: 535\nComposer: test2\nsongs: 5\nplaytime: 898\n"),
      Result.successful(SongCountList(
        SongCount(12, Seconds(535), Some(TagVal(Tag.Composer, "test1"))),
        SongCount(5, Seconds(898), Some(TagVal(Tag.Composer, "test2")))
      )))
    assertEquals(c.parseValue("Track: 1\nsongs: 1\nplaytime: 1055\nTrack: 2\nsongs: 1\nplaytime: 544\nTrack: 3\nsongs: 1\nplaytime: 465\n")
      , Result.successful(SongCountList(
        SongCount(1, Seconds(1055), Some(TagVal(Tag.Track, "1"))),
        SongCount(1, Seconds(544), Some(TagVal(Tag.Track, "2"))),
        SongCount(1, Seconds(465), Some(TagVal(Tag.Track, "3")))
      )))

    assertEquals(c.parseValue(""), Result.successful(SongCountList.Empty))
  }

  test("filter") {
    val c = Filter.codec

    assertEquals(c.parseValue("any classical artist bla"),
      Result.successful(Filter(FilterType.Anywhere -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla")))

    assertEquals(c.write(Filter(FilterType.Anywhere -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla")),
      Result.successful("any classical artist bla"))
  }

  test("find") {
    val c = Find.codec

    assertEquals(c.parseValue("find any classical composer sibelius")
      , Result.successful(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), None, None)))

    assertEquals(c.parseValue("find any classical composer sibelius sort artist")
      , Result.successful(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), Some(Sort(Tag.Artist)), None)))

    assertEquals(c.parseValue("find any classical composer sibelius sort artist window 1:3")
      , Result.successful(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), Some(Sort(Tag.Artist)), Some(Range(1, 3)))))

    assertEquals(c.write(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), None, None))
      , Result.successful("find any classical composer sibelius"))

    assertEquals(c.write(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), Some(Sort(Tag.Artist)), None))
      , Result.successful("find any classical composer sibelius sort artist"))

    assertEquals(c.write(Find(Filter(
        FilterType.Anywhere -> "classical"
          , FilterType.TagFilter(Tag.Composer) -> "sibelius"), Some(Sort(Tag.Artist)), Some(Range(1,3))))
      , Result.successful("find any classical composer sibelius sort artist window 1:3"))

  }

  test("list command") {
    val c = List.codec

    assertEquals(c.parseValue("list artist")
      , Result.successful(List(ListType.TagListType(Tag.Artist))))

    assertEquals(c.parseValue("list artist genre classical")
      , Result.successful(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical")))))

    assertEquals(c.parseValue("list artist genre classical artist bla")
      , Result.successful(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla")))))

    assertEquals(c.parseValue("list artist genre classical artist bla group album")
      , Result.successful(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla"))
          , Vector(Tag.Album))))

    assertEquals(c.parseValue("list artist genre classical artist bla group album artist")
      , Result.successful(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla"))
          , Vector(Tag.Album, Tag.Artist))))

    assertEquals(c.write(List(ListType.TagListType(Tag.Artist)))
      , Result.successful("list artist"))

    assertEquals(c.write(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical"))))
      , Result.successful("list artist genre classical"))

    assertEquals(c.write(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla"))
          , Vector(Tag.Album)))
      , Result.successful("list artist genre classical artist bla group album"))

    assertEquals(c.write(List(
        ListType.TagListType(Tag.Artist)
          , Some(Filter(FilterType.TagFilter(Tag.Genre) -> "classical", FilterType.TagFilter(Tag.Artist) -> "bla"))
          , Vector(Tag.Album, Tag.Artist)))
      , Result.successful("list artist genre classical artist bla group album artist"))
  }

  test("tagvallist") {
    val c = TagValList.codec

    assertEquals(c.parse("genre: classic\ngenre: rock\n")
      , Result.successful(ParseResult(TagValList(Vector(TagVal(Tag.Genre, "classic"), TagVal(Tag.Genre, "rock"))), "")))

    assertEquals(c.parseValue("genre: classic\nartist: test\n")
      , Result.successful(TagValList(Vector(TagVal(Tag.Genre, "classic"), TagVal(Tag.Artist, "test")))))

    assertEquals(c.parseValue("genre: classic\n")
      , Result.successful(TagValList(Vector(TagVal(Tag.Genre, "classic")))))

    assertEquals(c.parseValue("Composer:\nComposer: Czajkowski\nComposer: Felix Mendelssohn\nComposer: Jean Sibelius\n")
      , Result.successful(TagValList.from(Tag.Composer -> "", Tag.Composer -> "Czajkowski", Tag.Composer -> "Felix Mendelssohn", Tag.Composer -> "Jean Sibelius")))

    assertEquals(c.write(TagValList(Vector(TagVal(Tag.Genre, "classic"), TagVal(Tag.Genre, "rock"))))
      , Result.successful("genre: classic\ngenre: rock\n"))

    assertEquals(c.parseValue("Genre: Classical\nGenre: Meditative\nGenre: classical\n")
      , Result.successful(TagValList.from(
        Tag.Genre -> "Classical", Tag.Genre -> "Meditative", Tag.Genre -> "classical")))


    assertEquals(c.write(TagValList.from(
      Tag.Genre -> "Classical", Tag.Genre -> "Meditative", Tag.Genre -> "classical"))
      , Result.successful("genre: Classical\ngenre: Meditative\ngenre: classical\n"))

  }

  test("file and directory") {
    val c = File.codec

    assertEquals(c.parseValue("directory: some name\nLast-Modified: 2016-11-12T12:31:12Z\n")
      , Result.successful(File.Directory("some name", Instant.parse("2016-11-12T12:31:12Z"))))

    assertEquals(c.parseValue("file: some name\nsize: 21212\nLast-Modified: 2016-11-12T12:31:12Z\n")
      , Result.successful(File.Basic("some name", 21212L, Instant.parse("2016-11-12T12:31:12Z"))))

    assertEquals(c.write(File.Basic("some name", 21212L, Instant.parse("2016-11-12T12:31:12Z")))
      , Result.successful("file: some name\nsize: 21212\nLast-Modified: 2016-11-12T12:31:12Z\n"))

    assertEquals(c.write(File.Directory("some name", Instant.parse("2016-11-12T12:31:12Z")))
      , Result.successful("directory: some name\nLast-Modified: 2016-11-12T12:31:12Z\n"))
  }

  test("file list") {
    val c = FileList.codec

    val time = Instant.parse("2016-11-12T12:31:12Z")

    assertEquals(c.parseValue("file: some file name\nsize: 111\nLast-Modified: 2016-11-12T12:31:12Z\ndirectory: another name\nLast-Modified: 2016-11-12T12:31:12Z\n")
      , Result.successful(FileList(Vector(File.Basic("some file name", 111L, time), File.Directory("another name", time)))))
  }

  test("info list") {
    val c = InfoList.codec

    val testStr = """file: Various-New Eyes on Baroque/06.Air-Air.flac
                  |Last-Modified: 2018-05-01T17:57:55Z
                  |Time: 109
                  |duration: 109.026
                  |Artist: Air
                  |Album: New Eyes on Baroque
                  |Title: Air
                  |Date: 2013
                  |Genre: Classical
                  |Track: 6
                  |file: Various-New Eyes on Baroque/07.Air on a G-string-Air on a G-string.flac
                  |Last-Modified: 2018-05-01T17:58:14Z
                  |Time: 204
                  |duration: 204.360
                  |Artist: Air on a G-string
                  |Album: New Eyes on Baroque
                  |Title: Air on a G-string
                  |Date: 2013
                  |Genre: Classical
                  |Track: 7
                  |directory: just a directory
                  |Last-Modified: 2018-05-01T17:58:15Z
                  |file: Various-New Eyes on Baroque/08.Wachet auf-Wachet auf.flac
                  |Last-Modified: 2018-05-01T17:58:35Z
                  |Time: 228
                  |duration: 227.946
                  |Artist: Wachet auf
                  |Album: New Eyes on Baroque
                  |Title: Wachet auf
                  |Date: 2013
                  |Genre: Classical
                  |Track: 8
                  |playlist: Various-New Eyes on Baroque/New Eyes on Baroque.m3u
                  |Last-Modified: 2018-05-01T17:55:12Z
                  |""".stripMargin

    val Right(result) = c.parseValue(testStr)

    assertEquals(result.size, 5)
    assertEquals(result.items.collect({ case Info.DirInfo(d) => d })
      , Vector(File.Directory("just a directory", Instant.parse("2018-05-01T17:58:15Z"))))

    assertEquals(result.items.collect({case Info.SongInfo(s) => s.tags.get(Tag.Album) }).distinct
      , Vector(Some("New Eyes on Baroque")))

    assertEquals(result.items.collect({case Info.SongInfo(s) => s}).size, 3)

    assertEquals(result.items.collect({case Info.PlaylistInfo(p) => p})
      , Vector(PlaylistSummary(Uri("Various-New Eyes on Baroque/New Eyes on Baroque.m3u"), Instant.parse("2018-05-01T17:55:12Z"))))

  }

  test("sticker find answer") {
    val c = StickerFindAnswer.codec

    assertEquals(c.parseValue("file: path/to/flac\nsticker: played=2\nfile: path/to/ogg\nsticker: played=20\n")
      , Result.successful(StickerFindAnswer(Vector(
        StickerFile("path/to/flac", Sticker("played", "2"))
          , StickerFile("path/to/ogg", Sticker("played", "20"))))))

    assertEquals(c.write(StickerFindAnswer(Vector(
        StickerFile("path/to/flac", Sticker("played", "2"))
          , StickerFile("path/to/ogg", Sticker("played", "20")))))
      , Result.successful("file: path/to/flac\nsticker: played=2\nfile: path/to/ogg\nsticker: played=20\n"))
  }

  test("output") {
    val c = Output.codec

    assertEquals(c.parseValue("outputid: 0\noutputname: FIIO X5\noutputenabled: 1\n")
      , Result.successful(Output(Id("0"), "FIIO X5", true)))

    assertEquals(c.write(Output(Id("0"), "FIIO X5", true))
      , Result.successful("outputid: 0\noutputname: FIIO X5\noutputenabled: 1\n"))
  }

  test("decoders answer") {
    val c = DecodersAnswer.codec
    val str = """plugin: mad
                |suffix: mp3
                |suffix: mp2
                |mime_type: audio/mpeg
                |plugin: mpg123
                |suffix: mp3
                |plugin: vorbis
                |suffix: ogg
                |suffix: oga
                |mime_type: application/ogg
                |mime_type: application/x-ogg
                |mime_type: audio/ogg
                |mime_type: audio/vorbis
                |mime_type: audio/vorbis+ogg
                |mime_type: audio/x-ogg
                |mime_type: audio/x-vorbis
                |mime_type: audio/x-vorbis+ogg
                |plugin: oggflac
                |suffix: ogg
                |suffix: oga
                |mime_type: application/ogg
                |mime_type: application/x-ogg
                |mime_type: audio/ogg
                |mime_type: audio/x-flac+ogg
                |mime_type: audio/x-ogg
                |plugin: flac
                |suffix: flac
                |mime_type: application/flac
                |mime_type: application/x-flac
                |mime_type: audio/flac
                |mime_type: audio/x-flac
                |""".stripMargin

    assertEquals(c.parseValue(str)
      , Result.successful(DecodersAnswer(Vector(
        DecoderPlugin("mad", Vector("mp3", "mp2"), Vector("audio/mpeg"))
          , DecoderPlugin("mpg123", Vector("mp3"), Vector())
          , DecoderPlugin("vorbis", Vector("ogg", "oga")
            , Vector("application/ogg", "application/x-ogg", "audio/ogg"
              , "audio/vorbis", "audio/vorbis+ogg", "audio/x-ogg", "audio/x-vorbis", "audio/x-vorbis+ogg"))
          , DecoderPlugin("oggflac",Vector("ogg", "oga")
            , Vector("application/ogg", "application/x-ogg", "audio/ogg", "audio/x-flac+ogg", "audio/x-ogg"))
          , DecoderPlugin("flac",Vector("flac")
            , Vector("application/flac", "application/x-flac", "audio/flac", "audio/x-flac"))))))
  }

  test("message") {
    val c = Message.codec
    assertEquals(c.parseValue("channel: test\nmessage: hello\n"), Result.successful(Message("test", "hello")))
    assertEquals(c.write(Message("test", "hello")), Result.successful("channel: test\nmessage: hello\n"))
  }

  test("search") {
    val c = Search.codec

    assertEquals(c.parseValue("search genre classical any bla sort -albumartist window 1:3")
      , Result.successful(Search(Filter(
        FilterType.TagFilter(Tag.Genre) -> "classical",
        FilterType.Anywhere -> "bla")
        , Some(Sort(true, FilterType.TagFilter(Tag.Albumartist)))
        , Some(Range(1, 3)))))

    assertEquals(c.write(Search(Filter(
        FilterType.TagFilter(Tag.Genre) -> "classical",
        FilterType.Anywhere -> "bla")
        , Some(Sort(true, FilterType.TagFilter(Tag.Albumartist)))
      , Some(Range(1, 3))))
      , Result.successful("search genre classical any bla sort -albumartist window 1:3"))
  }

  test("range") {
    val c = Range.codec

    assertEquals(c.parseValue("1:2"), Result.successful(Range(1,2)))
    assertEquals(c.write(Range(2,3)), Result.successful("2:3"))
  }

  test("delete") {
    val c = Delete.codec

    assertEquals(c.parseFull("delete 0:5"), Result.successful(ParseResult(Delete(Left(Range(0,5))), "")))
    assertEquals(c.parseValue("delete 5"), Result.successful(Delete(Right(5))))

    val cc = CommandCodec.defaultCodec
    assertEquals(cc.parseValue("delete 0:5"), Result.successful(Delete(Left(Range(0,5)))))
  }

  test("stats answer") {
    val c = StatsAnswer.codec

    assertEquals(c.parseValue("uptime: 32485\nplaytime: 49\nartists: 217\nalbums: 183\nsongs: 2869\ndb_playtime: 809117\ndb_update: 1533479221\n")
      , Result.successful(StatsAnswer(217, 183, 2869, Seconds(32485), 809117, 1533479221, 49)))
  }
}
