package org.matthicks.media4s.video.filter

case class FPSFilter(framesPerSecond: Double) extends VideoFilter {
  val value = s"fps=$framesPerSecond"
}
