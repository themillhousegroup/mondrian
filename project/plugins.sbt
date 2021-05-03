// Comment out all non-essential plugins while waiting for
// SBT 1.0 / Scala 2.12 versions to be published;

// addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.3.0")

// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")

// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

//resolvers += Resolver.url(
//  "bintray-sbt-plugin-releases",
//    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
//        Resolver.ivyStylePatterns)
//
//addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")
//

// addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")


// Uncomment if this is a Scala 2.11(+) project to get scapegoat linting:
// addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "0.94.6")


addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")
