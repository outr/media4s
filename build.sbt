name := "media4s"
organization := "org.matthicks"
version := "1.0.15-SNAPSHOT"
scalaVersion := "2.13.2"
crossScalaVersions := List("2.13.2", "2.12.11")
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
    "io.youi" %% "youi-core" % "0.13.15",
    "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % "test"
)

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "org.matthicks"
publishMavenStyle in ThisBuild := true
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/media4s/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "media4s", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/media4s"))
scmInfo in ThisBuild := Some(
    ScmInfo(
        url("https://github.com/outr/media4s"),
        "scm:git@github.com:outr/media4s.git"
    )
)
developers in ThisBuild := List(
    Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)
