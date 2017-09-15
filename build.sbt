name := "media4s"
organization := "org.matthicks"
version := "1.0.12"
scalaVersion := "2.12.3"
crossScalaVersions := List("2.12.3", "2.11.11")
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
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard")

libraryDependencies += "org.im4java" % "im4java" % "1.4.0"
libraryDependencies += "org.powerscala" %% "powerscala-io" % "2.0.5"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % "0.8.0")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

testOptions in Test += Tests.Argument("-oDF")
