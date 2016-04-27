import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object ApplicationBuild extends Build {

  lazy val appSettings = Seq(

    name := "hibernate-sequences",
    organization := "com.kainos",
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
    crossPaths := false,
    mainClass in Compile := Some("com.kainos.learn.hibseq.HibernateSequences"),
    jarName in assembly := "hibernate-sequences.jar",

    resolvers ++= Seq(
      //"Coda Hale repo" at "http://repo.codahale.com/",
      "sonatype" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      "org.dbunit" % "dbunit" % "2.5.0" % "test",
      "io.dropwizard" % "dropwizard-core" % "0.9.2",
      "io.dropwizard" % "dropwizard-hibernate" % "0.9.2",
      "io.dropwizard" % "dropwizard-client" % "0.9.2",
      "io.dropwizard" % "dropwizard-testing" % "0.9.2",
//      "io.dropwizard" % "dropwizard-core" % "0.7.2",
//      "io.dropwizard" % "dropwizard-hibernate" % "0.7.2",
//      "io.dropwizard" % "dropwizard-client" % "0.7.2",
//      "io.dropwizard" % "dropwizard-testing" % "0.7.2",
      "org.modelmapper"  %   "modelmapper" %   "0.6.2",
      "org.postgresql" % "postgresql" % "9.4.1208.jre7"
    )
  )

  lazy val root = Project(
    id = "hibernate-sequences",
    base = file("."),
    settings = appSettings)

}
