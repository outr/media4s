package org.matthicks.media4s.video.info

import java.text.SimpleDateFormat

import com.outr.scribe.Logging
import org.matthicks.media4s.video.MetaData
import rapture.json._
import rapture.json.jsonBackends.jackson._

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class MediaInfo(duration: Double,
                     start: Double,
                     bitRate: Long,
                     videoInfo: Option[VideoInfo],
                     audioInfo: Option[AudioInfo],
                     meta: MetaData = MetaData()) {
  def video = videoInfo.getOrElse(throw new RuntimeException("No video stream available."))
  def audio = audioInfo.getOrElse(throw new RuntimeException("No audio stream available."))
  def frames = videoInfo.map(v => (v.fps * duration).toInt).getOrElse(0)

  def hasVideo = videoInfo.nonEmpty
  def hasAudio = audioInfo.nonEmpty

  override def toString = {
    s"MediaInfo(duration: $duration, start: $start, video: $video, audio: $audio, frames: $frames, meta: $meta)"
  }
}

object MediaInfo extends Logging {
  def apply(jsonString: String): MediaInfo = {
    val json = Json.parse(jsonString)
    var videoInfo: Option[VideoInfo] = None
    var audioInfo: Option[AudioInfo] = None
    json.streams.as[List[Json]].indices.foreach { index =>
      val stream = json.streams(index)
      stream.codec_type.as[String] match {
        case "video" => {
          val averageFrameRate = stream.avg_frame_rate.as[String]
          val fps = averageFrameRate.substring(0, averageFrameRate.indexOf('/')).toDouble
          val video = VideoInfo(
            codec = stream.codec_name.as[String],
            width = stream.width.as[Int],
            height = stream.height.as[Int],
            fps = fps,
            tags = Tags(stream.tags.as[Map[String, String]])
          )
          if (videoInfo.nonEmpty) throw new RuntimeException("Multiple video formats detected!")
          videoInfo = Some(video)
        }
        case "audio" => {
          val audio = AudioInfo(
            codec = stream.codec_name.as[String],
            bitRate = stream.bit_rate.as[String].toLong,
            channels = stream.channels.as[Int],
            channelLayout = stream.channel_layout.as[String],
            tags = Tags(stream.tags.as[Map[String, String]])
          )
          if (audioInfo.nonEmpty) throw new RuntimeException("Multiple audio formats detected!")
          audioInfo = Some(audio)
        }
        case codecType => throw new RuntimeException(s"Unsupported codec_type: $codecType.")
      }
    }
    MediaInfo(
      duration = json.format.duration.as[String].toDouble,
      start = json.format.start_time.as[String].toDouble,
      bitRate = json.format.bit_rate.as[String].toLong,
      videoInfo = videoInfo,
      audioInfo = audioInfo
    )
  }
}