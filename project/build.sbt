libraryDependencies ++= Seq(
  // elm plugin: minify elm js file
  "com.google.javascript" % "closure-compiler" % "v20180805",

  // webjar plugin
  "org.apache.tika" % "tika-core" % "1.18"
  // circe is pulled in by sbt-microsites plugin
  // "io.circe" %% "circe-core" % "0.9.3",
  // "io.circe" %% "circe-generic" % "0.9.3"
)