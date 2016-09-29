package org.matthicks.media4s.video

case class ScaleFilter(width: Int, height: Int) extends VideoFilter {
  val value = s"scale=$width:$height"
}
