package org.matthicks.media4s.video

sealed abstract class VideoCodec(val value: String)

object VideoCodec {
  case object copy extends VideoCodec("copy")
  case object libx264 extends VideoCodec("libx264")
  case object libvpx extends VideoCodec("libvpx")
  case object mjpeg extends VideoCodec("mjpeg")

  val values = Vector(copy, libx264, libvpx, mjpeg)
}