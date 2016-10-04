package org.matthicks.media4s.video.codec

sealed abstract class VideoCodec(val value: String)

object VideoCodec {
  case object copy extends VideoCodec("copy")
  case object libx264 extends VideoCodec("libx264")
  case object libvpx extends VideoCodec("libvpx")
  case object libvpx_vp9 extends VideoCodec("libvpx-vp9")
  case object mjpeg extends VideoCodec("mjpeg")

  val values = Vector(copy, libx264, libvpx, mjpeg)
}