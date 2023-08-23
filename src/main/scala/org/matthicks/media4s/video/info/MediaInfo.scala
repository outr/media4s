package org.matthicks.media4s.video.info

import fabric.rw._

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class MediaInfo(streams: List[MediaStream], format: MediaFormat) {
  def duration: Double = format.duration.toDouble
  def start: Double = format.start_time
  def bitRate: Long = format.bit_rate

  lazy val videos: List[VideoInfo] = streams.collect {
    case s if s.codec_type == "video" => {
      val frameRate = s.r_frame_rate
      val fps = frameRate.substring(0, frameRate.indexOf('/')).toDouble
      VideoInfo(
        codec = s.codec_name.getOrElse(""),
        width = s.width,
        height = s.height,
        fps = fps,
        tags = Tags(Map.empty)
      )
    }
  }
  lazy val audios: List[AudioInfo] = streams.collect {
    case s if s.codec_type == "audio" => {
      AudioInfo(
        codec = s.codec_name.getOrElse(""),
        bitRate = s.bit_rate,
        channels = s.channels,
        channelLayout = Option(s.channel_layout),
        tags = Tags(Map.empty)
      )
    }
  }
  def video: VideoInfo = videos.head
  def audio: Option[AudioInfo] = audios.headOption
  def frames: Int = videos.headOption.map(_.fps * duration).map(_.toInt).getOrElse(0)

  def hasVideo: Boolean = videos.nonEmpty
  def hasAudio: Boolean = audios.nonEmpty

  override def toString: String = {
    val audioString = audio.fold("No Audio")(audio => s"$audio")
    s"MediaInfo(duration: $duration, start: $start, video: $video, $audioString, frames: $frames)"
  }
}

object MediaInfo {
  implicit val rw: RW[MediaInfo] = RW.gen
}
