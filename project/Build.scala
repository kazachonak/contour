import sbt._
import com.github.siasia._
import PluginKeys._
import Keys._

object Build extends Build {
  import BuildSettings._
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Root Project
  // -------------------------------------------------------------------------------------------------------------------

  lazy val root = Project("root",file("."))
    .aggregate(examples, contourCommon, contourClient, contourServer, contourTesting)
    .settings(basicSettings: _*)


  // -------------------------------------------------------------------------------------------------------------------
  // Modules
  // -------------------------------------------------------------------------------------------------------------------

  lazy val contourCommon = Project("contour-common", file("contour-common"))
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++=
      compile(reactive) ++
      test(specs2, jUnit)
    )

  lazy val contourClient = Project("contour-client", file("contour-client"))
    .dependsOn(contourCommon)
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++=
      compile(gwtUser, gwtDev, gwtQuery) ++
      test(specs2, jUnit)
    )


  lazy val contourServer = Project("contour-server", file("contour-server"))
    .dependsOn(contourCommon)
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++=
      compile(slf4j) ++
      runtime(logback) ++
      test(specs2, jUnit)
    )

  lazy val contourTesting = Project("contour-testing", file("contour-testing"))
    .dependsOn(contourClient, contourServer, contourCommon)
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++=
      test(specs2, jUnit)
    )

  // -------------------------------------------------------------------------------------------------------------------
  // Example Projects
  // -------------------------------------------------------------------------------------------------------------------

  lazy val examples = Project("examples", file("examples"))
    .aggregate(stockwatcher, stockwatcher2)
    .settings(exampleSettings: _*)

  lazy val stockwatcher = Project("stockwatcher", file("examples/stockwatcher"))
    .settings(exampleSettings: _*)
    .settings(libraryDependencies ++=
      compile(gwtUser, gwtDev) ++
      test(specs2, jUnit)
    )

  lazy val stockwatcher2 = Project("stockwatcher2", file("examples/stockwatcher2"))
    .dependsOn(contourClient, contourCommon)
    .settings(exampleSettings: _*)
    .settings(libraryDependencies ++=
      compile(gwtUser, gwtDev) ++
      test(specs2, jUnit)
    )
}
