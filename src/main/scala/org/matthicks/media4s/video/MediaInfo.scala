package org.matthicks.media4s.video

import java.util.Calendar

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

case class VideoInfo(codec: String = null, width: Int = 0, height: Int = 0, fps: Double = 0.0, meta: MetaData = MetaData()) {
  override def toString = {
    s"VideoInfo(codec: $codec, width: $width, height: $height, fps: $fps, meta: $meta)"
  }
}

case class AudioInfo(codec: String = null, range: Long = 0L, stereo: Boolean = false, meta: MetaData = MetaData()) {
  override def toString = {
    s"AudioInfo(codec: $codec, range: $range, stereo: $stereo, meta: $meta)"
  }
}

case class MetaData(map: Map[String, Any] = Map.empty) {
  def apply(key: String, value: String) = copy(map + (key -> MetaData.fromString(value)))

  def get[T](key: String) = map.get(key).asInstanceOf[Option[T]]
  def apply[T](key: String) = map(key).asInstanceOf[T]

  override def toString = map.map(t => s"${t._1} = ${t._2}").mkString(", ")
}

object MetaData {
  private val Integer = """\d+""".r
  private val Double = """[0-9.-]+""".r
  private val Date = """(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})""".r

  def fromString(s: String) = s match {
    case Integer(value) => value.toInt
    case Double(value) => value.toDouble
    case Date(year, month, day, hour, minute, second) => {
      val calendar = Calendar.getInstance()
      calendar.set(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt)
      calendar.getTimeInMillis
    }
    case _ => s
  }
}