package org.matthicks.media4s.video

sealed abstract class VideoProfile(val value: String)

object VideoProfile {
  case object Baseline extends VideoProfile("baseline")
  case object Main extends VideoProfile("main")
  case object High extends VideoProfile("high")

  val values = Vector(Baseline, Main, High)
}