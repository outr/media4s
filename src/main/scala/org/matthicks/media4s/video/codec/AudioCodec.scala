package org.matthicks.media4s.video.codec

sealed abstract class AudioCodec(val value: String)

object AudioCodec {
  case object copy extends AudioCodec("copy")
  case object libfdkAAC extends AudioCodec("libfdk_aac")
  case object libmp3Lame extends AudioCodec("libmp3lame")
  case object libvorbis extends AudioCodec("libvorbis")

  val values = Vector(copy, libfdkAAC, libmp3Lame, libvorbis)
}