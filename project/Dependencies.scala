import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    ScalaToolsSnapshots,
    "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
    "kazachonak repo" at "http://kazachonak.github.com/maven-repo/releases"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  object V {
    val jetty    = "8.1.0.v20120127"
  }

  val logback     = "ch.qos.logback"    %  "logback-classic"   % "1.0.0"
  val slf4j       = "org.slf4j"         %  "slf4j-api"         % "1.6.4"
  val specs2      = "org.specs2"        %% "specs2"            % "1.9"
  val jUnit       = "junit"             %  "junit"             % "4.8"
  val gwtUser     = "com.google.gwt"    %  "gwt-user"          % "2.4.0"
  val gwtDev      = "com.google.gwt"    %  "gwt-dev"           % "2.4.0"
  val gwtQuery    = "com.googlecode.gwtquery" % "gwtquery"     % "1.1.0"
  val reactive    = "reactive"          %  "core"              % "0.6-gwt"
  val jettyWebApp = "org.eclipse.jetty" %  "jetty-webapp"      % V.jetty
}
