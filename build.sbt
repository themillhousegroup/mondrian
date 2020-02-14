name := "mondrian"

// If the CI supplies a "build.version" environment variable, inject it as the rev part of the version number:
version := s"${sys.props.getOrElse("build.majorMinor", "0.8")}.${sys.props.getOrElse("build.version", "SNAPSHOT")}"

scalaVersion := "2.12.10"

organization := "com.themillhousegroup"

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

resolvers ++= Seq(  "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
                    "oss-releases" at "https://oss.sonatype.org/content/repositories/releases",
										"Millhouse Bintray" at "http://dl.bintray.com/themillhousegroup/maven",
                    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

// jacoco.settings

// seq(bintraySettings:_*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// net.virtualvoid.sbt.graph.Plugin.graphSettings

