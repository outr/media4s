name := "media4s"
organization := "org.matthicks"
version := "1.0.6"
scalaVersion := "2.11.8"
sbtVersion := "0.13.13"
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

libraryDependencies += "com.outr" %% "scribe" % "1.3.2"
libraryDependencies += "org.im4java" % "im4java" % "1.4.0"
libraryDependencies += "com.propensive" %% "rapture-json-jackson" % "2.0.0-M7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

testOptions in Test += Tests.Argument("-oDF")
