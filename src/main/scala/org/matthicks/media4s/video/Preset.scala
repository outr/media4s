package org.matthicks.media4s.video

sealed abstract class Preset(val value: String)

object Preset {
  case object UltraFast extends Preset("ultrafast")
  case object SuperFast extends Preset("superfast")
  case object VeryFast extends Preset("veryfast")
  case object Faster extends Preset("faster")
  case object Fast extends Preset("fast")
  case object Medium extends Preset("medium")
  case object Slow extends Preset("slow")
  case object Slower extends Preset("slower")
  case object VerySlow extends Preset("veryslow")
  case object Placebo extends Preset("placebo")

  val values = Vector(UltraFast, SuperFast, VeryFast, Faster, Fast, Medium, Slow, Slower, VerySlow, Placebo)
}