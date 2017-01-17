package org.matthicks.media4s.video.info

import com.outr.scribe.Logging
import org.matthicks.media4s.video.MetaData
import io.circe.optics.JsonPath._
import cats.syntax.either._
import io.circe._
import io.circe.parser._

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
  def apply(jsonString: String): MediaInfo = try {
    val json = parse(jsonString).getOrElse(throw new RuntimeException(s"Unable to parse: $jsonString"))
    var videoInfo: Option[VideoInfo] = None
    var audioInfo: Option[AudioInfo] = None
    root.streams.each.json.getAll(json).foreach { stream =>
      root.codec_type.string.getOption(stream).get match {
        case "video" => {
          val averageFrameRate = root.avg_frame_rate.string.getOption(stream).get
          val fps = averageFrameRate.substring(0, averageFrameRate.indexOf('/')).toDouble
          val video = VideoInfo(
            codec = root.codec_name.string.getOption(stream).get,
            width = root.width.int.getOption(stream).get,
            height = root.height.int.getOption(stream).get,
            fps = fps,
            tags = Tags(root.tags.as[Map[String, String]].getOption(stream).getOrElse(Map.empty))
          )
          if (videoInfo.nonEmpty) throw new RuntimeException("Multiple video formats detected!")
          videoInfo = Some(video)
        }
        case "audio" => {
          val audio = AudioInfo(
            codec = root.codec_name.string.getOption(stream).get,
            bitRate = root.bit_rate.string.getOption(stream).map(_.toLong).getOrElse(0L),
            channels = root.channels.int.getOption(stream).getOrElse(0),
            channelLayout = root.channel_layout.string.getOption(stream).get,
            tags = Tags(root.tags.as[Map[String, String]].getOption(stream).getOrElse(Map.empty))
          )
          if (audioInfo.nonEmpty) throw new RuntimeException("Multiple audio formats detected!")
          audioInfo = Some(audio)
        }
        case codecType => throw new RuntimeException(s"Unsupported codec_type: $codecType.")
      }
    }
    MediaInfo(
      duration = root.format.duration.string.getOption(json).map(_.toDouble).get,
      start = root.format.start_time.string.getOption(json).map(_.toDouble).getOrElse(0.0),
      bitRate = root.format.bit_rate.string.getOption(json).map(_.toLong).getOrElse(0L),
      videoInfo = videoInfo,
      audioInfo = audioInfo
    )
  } catch {
    case t: Throwable => throw new RuntimeException(s"Failed to process: $jsonString", t)
  }
}
