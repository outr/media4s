package org.matthicks.media4s.video.info

import org.matthicks.media4s.Size

case class VideoInfo(codec: String,
                     width: Int,
                     height: Int,
                     fps: Double,
                     tags: Tags) extends Size {
  override def toString = {
    s"VideoInfo(codec: $codec, width: $width, height: $height, fps: $fps, tags: $tags)"
  }
}