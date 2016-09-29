package org.matthicks.media4s.video

case class CropFilter(width: Int, height: Int, x: Int = 0, y: Int = 0) extends VideoFilter {
  val value = s"crop=$width:$height:$x:$y"
}
