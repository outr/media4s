package org.matthicks.media4s.video.info

case class AudioInfo(codec: String = null, bitRate: Long = 0L, channels: Int, channelLayout: Option[String], tags: Tags) {
  override def toString = {
    s"AudioInfo(codec: $codec, bitRate: $bitRate, channels: $channels, channelsLayout: $channelLayout, tags: $tags)"
  }
}