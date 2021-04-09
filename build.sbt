name := "tinkl-shopify"

version := "0.1-SNAPSHOT"

scalaVersion := "2.13.5"

enablePlugins(PlayScala)

libraryDependencies ++= Seq(guice, ws)

libraryDependencies ++= Seq(
  "ai.x"         %% "play-json-extensions" % "0.40.2",
  "com.beachape" %% "enumeratum-play"      % "1.6.3",
  "commons-codec" % "commons-codec"        % "1.15"
)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"

Compile / herokuAppName := "tinkl-shopify"