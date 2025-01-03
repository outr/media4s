name := "media4s"
organization := "com.outr"
version := "1.0.23"
scalaVersion := "2.13.15"
crossScalaVersions := List("2.13.15", "3.3.4")
fork := true
scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked")

scalacOptions ++= (scalaVersion.value match {
    case s if s.startsWith("3.") => Seq(
        "-Xmax-inlines", "128"
    )
    case s => Seq(
        "-Xlint",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard"
    )
})
Test / testOptions += Tests.Argument("-oDF")

libraryDependencies ++= List(
    "org.im4java" % "im4java" % "1.4.0",
    "com.outr" %% "spice-core" % "0.7.2",
    "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    "org.scalatest" %% "scalatest" % "3.2.19" % "test"
)
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeProfileName := "com.outr"
publishMavenStyle := true
ThisBuild / licenses := Seq("MIT" -> url("https://github.com/outr/media4s/blob/master/LICENSE"))
ThisBuild / sonatypeProjectHosting := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "media4s", "matt@outr.com"))
ThisBuild / homepage := Some(url("https://github.com/outr/media4s"))
ThisBuild / scmInfo := Some(
    ScmInfo(
        url("https://github.com/outr/media4s"),
        "scm:git@github.com:outr/media4s.git"
    )
)
ThisBuild / developers := List(
    Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)
