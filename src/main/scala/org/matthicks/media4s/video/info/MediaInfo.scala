package org.matthicks.media4s.video.info

import profig.JsonUtil

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
        codec = s.codec_name,
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
        codec = s.codec_name,
        bitRate = s.bit_rate,
        channels = s.channels,
        channelLayout = Option(s.channel_layout),
        tags = Tags(Map.empty)
      )
    }
  }
  def video: VideoInfo = videos.head
  def audio: AudioInfo = audios.head
  def frames: Int = videos.headOption.map(_.fps * duration).map(_.toInt).getOrElse(0)

  def hasVideo: Boolean = videos.nonEmpty
  def hasAudio: Boolean = audios.nonEmpty

  override def toString: String = {
//    s"MediaInfo(duration: $duration, start: $start, video: $video, audio: $audio, frames: $frames, meta: $meta)"
    "STRING"
  }
}

case class MediaStream(index: Int,
                       codec_name: String,
                       codec_long_name: String,
                       profile: String = "",
                       codec_type: String,
                       codec_time_base: String,
                       codec_tag_string: String,
                       codec_tag: String,
                       width: Int = 0,
                       height: Int = 0,
                       coded_width: Int = 0,
                       coded_height: Int = 0,
                       has_b_frames: Int = 0,
                       sample_fmt: String = "",
                       sample_rate: Long = 0L,
                       channels: Int = 0,
                       channel_layout: String = "",
                       sample_aspect_ratio: String = "",
                       display_aspect_ratio: String = "",
                       pix_fmt: String = "",
                       level: Int = 0,
                       chroma_location: String = "",
                       field_order: String = "",
                       refs: Int = 0,
                       is_avc: Boolean = false,
                       nal_length_size: Int = 0,
                       r_frame_rate: String,
                       avg_frame_rate: String,
                       time_base: String,
                       duration_ts: Long = 0L,
                       duration: BigDecimal = BigDecimal(0),
                       start_time: BigDecimal = BigDecimal(0),
                       bit_rate: Long = 0L,
                       bits_per_sample: Int = 0,
                       bits_per_raw_sample: Int = 0)

case class MediaFormat(filename: String,
                       start_time: Double = 0.0,
                       nb_streams: Int,
                       nb_programs: Int,
                       format_name: String,
                       format_long_name: String,
                       duration: BigDecimal,
                       size: Long,
                       bit_rate: Long,
                       probe_score: Int,
                       tags: Map[String, String])

object MediaInfo {
  def apply(jsonString: String): MediaInfo = try {
    JsonUtil.fromJsonString[MediaInfo](jsonString)
  } catch {
    case t: Throwable => throw new RuntimeException(s"Failed to parse: $jsonString", t)
  }
  /*def apply(jsonString: String): MediaInfo = try {
    val json = parse(jsonString).right.getOrElse(throw new RuntimeException(s"Unable to parse: $jsonString"))
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
            channelLayout = root.channel_layout.string.getOption(stream),
            tags = Tags(root.tags.as[Map[String, String]].getOption(stream).getOrElse(Map.empty))
          )
          if (audioInfo.nonEmpty) throw new RuntimeException("Multiple audio formats detected!")
          audioInfo = Some(audio)
        }
        case "data" => {}
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
  }*/
}
