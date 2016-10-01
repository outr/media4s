package org.matthicks.media4s.video.info

import org.matthicks.media4s.Info

case class VideoInfo(codec: String,
                     width: Int,
                     height: Int,
                     fps: Double,
                     tags: Tags) extends Info {
  override def toString = {
    s"VideoInfo(codec: $codec, width: $width, height: $height, fps: $fps, tags: $tags)"
  }
}