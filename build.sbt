name := "mondrian"

organization := "com.themillhousegroup"

developers := List(
  Developer(id="themillhousegroup", name="John Marshall", email="john@themillhousegroup.com", url=url("http://www.themillhousegroup.com"))
)

homepage := Some(url("https://github.com/themillhousegroup/mondrian"))

scalaVersion := "2.12.10"

val targetPlayReactiveMongoVersion = "0.12.6-play26" 

val targetPlayVersion = "2.6.12"

val minimumSpecs2Version = "[4.8.3,)"

libraryDependencies ++= Seq(
    "org.reactivemongo"       %%    "play2-reactivemongo"       % targetPlayReactiveMongoVersion,
    "com.typesafe.play"       %%    "play"                      % targetPlayVersion                             % "provided",
    "com.typesafe.play"       %%    "play-cache"                % targetPlayVersion                             % "provided",
    "com.typesafe.play"       %%    "play-json"                 % targetPlayVersion                             % "provided",
    "org.mockito"             %     "mockito-all"               % "1.10.19"                                     % "test",
    "org.specs2"              %%    "specs2-core"               % minimumSpecs2Version                          % "test",
		"org.specs2"              %%  	"specs2-mock"           		% minimumSpecs2Version      										% "test"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// net.virtualvoid.sbt.graph.Plugin.graphSettings

// For all Sonatype accounts created on or after February 2021
sonatypeCredentialHost := "s01.oss.sonatype.org"

// Only for non-SNAPSHOT releases
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

