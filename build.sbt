name := "mondrian"

// If the CI supplies a "build.version" environment variable, inject it as the rev part of the version number:
version := s"${sys.props.getOrElse("build.majorMinor", "0.4")}.${sys.props.getOrElse("build.version", "SNAPSHOT")}"

scalaVersion := "2.11.7"

organization := "com.themillhousegroup"

val targetPlayReactiveMongoVersion = "0.11.11"

val targetPlayVersion = "2.5.3"

val minimumSpecs2Version = "[3.6,)"

libraryDependencies ++= Seq(
    "org.reactivemongo"       %%    "play2-reactivemongo"       % targetPlayReactiveMongoVersion,
    "com.typesafe.play"       %%    "play"                      % targetPlayVersion                             % "provided",
    "com.typesafe.play"       %%    "play-cache"                % targetPlayVersion                             % "provided",
    //"io.netty"              %     "netty"                     % "3.10.4.Final"                                % "provided",
    "org.mockito"             %     "mockito-all"               % "1.10.19"                                     % "test",
    "org.specs2"              %%    "specs2"                    % minimumSpecs2Version                          % "test",
    "com.themillhousegroup"   %%    "play2-reactivemongo-mocks" % s"${targetPlayReactiveMongoVersion}_0.7.40"   % "test"
)

resolvers ++= Seq(  "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
                    "oss-releases" at "https://oss.sonatype.org/content/repositories/releases",
										"Millhouse Bintray" at "http://dl.bintray.com/themillhousegroup/maven",
                    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

jacoco.settings

// publishArtifact in (Compile, packageDoc) := false

seq(bintraySettings:_*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

net.virtualvoid.sbt.graph.Plugin.graphSettings

