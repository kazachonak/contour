import sbt._
import Keys._
import com.github.siasia.WebPlugin._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.0.1-alpha",
    homepage              := Some(new URL("http://contourweb.org")),
    organization          := "contourweb.org",
    organizationHomepage  := Some(new URL("http://contourweb.org")),
    description           := "Contour",
    startYear             := Some(2012),
    licenses              := Seq("GNU LGPL" -> new URL("http://www.gnu.org/licenses/lgpl.html")),
    scalaVersion          := "2.9.1",
    resolvers             ++= Dependencies.resolutionRepos,
    scalacOptions         := Seq("-deprecation", "-encoding", "utf8", "-Ydependent-method-types")
  )

  lazy val moduleSettings = basicSettings

  lazy val exampleSettings = basicSettings
}