package org.matthicks.media4s.video

case class VideoInfo(codec: String = null, width: Int = 0, height: Int = 0, fps: Double = 0.0, meta: MetaData = MetaData()) {
  override def toString = {
    s"VideoInfo(codec: $codec, width: $width, height: $height, fps: $fps, meta: $meta)"
  }
}
