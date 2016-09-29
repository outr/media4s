package org.matthicks.media4s.video.info

import org.matthicks.media4s.video.MetaData

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class MediaInfo(created: Long = 0L,
                     duration: Double = 0.0,  // in seconds
                     start: Double = 0.0,
                     bitRate: Long = 0L,
                     video: VideoInfo = null,
                     audio: AudioInfo = null,
                     meta: MetaData = MetaData()) {
  def frames = if (video != null) math.round(video.fps * duration).toInt else 0

  def hasVideo = video != null
  def hasAudio = audio != null

  override def toString = {
    s"MediaInfo(duration: $duration, start: $start, video: $video, audio: $audio, frames: $frames, meta: $meta)"
  }
}