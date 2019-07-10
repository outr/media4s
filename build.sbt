name := "media4s"
organization := "org.matthicks"
version := "1.0.13-SNAPSHOT"
scalaVersion := "2.13.0"
crossScalaVersions := List("2.13.0", "2.12.8", "2.11.12")
fork := true
scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard")
testOptions in Test += Tests.Argument("-oDF")

libraryDependencies ++= List(
    "org.im4java" % "im4java" % "1.4.0",
    "io.youi" %% "youi-core" % "0.11.13",
    "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
    "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % "test"
)
