package org.matthicks.media4s.video

import java.io.File
import java.text.SimpleDateFormat

import com.outr.scribe.Logging
import org.matthicks.media4s.video.codec.{AudioCodec, VideoCodec}
import org.matthicks.media4s.video.filter.{CropFilter, ScaleFilter}
import org.matthicks.media4s.video.info.{AudioInfo, MediaInfo, VideoInfo}
import org.matthicks.media4s.video.transcode.Transcode
import org.powerscala.concurrent.Time._

import scala.sys.process._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object VideoUtil extends Logging {
  private val CreationTime = """creation_time\s*:\s*(.+)""".r
  private val DurationStartAndBitrate = """Duration: ([0-9]{2}):([0-9]{2}):([0-9]{2}).([0-9]{2}), start: ([0-9.]+), bitrate: ([0-9,.]+) kb/s""".r
  private val DurationStartAndBitrateShort = """Duration: ([0-9]{2}):([0-9]{2}):([0-9]{2}).([0-9]{2}), bitrate: ([0-9,.]+) kb/s""".r
  private val VideoDetails = """Stream #\d[.:]\d\(und\): Video: (.+?), (.+?), (\d+)x(\d+)(.*?), (\d+) kb/s, .*?([0-9.]+) fps.*""".r
  private val AudioDetails = """Stream #\d[.:]\d(?:\(und\))?: Audio: (\S+)( [(].+[)])?,.*? (\d+) Hz, (.+?),.*""".r
  private val MetaDataRegex = """(\S+)\s*:\s*(.+)""".r

  def info(file: File) = {
    val dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val filename = file.getAbsolutePath
    var mediaInfo = MediaInfo()
    var mediaMeta = MetaData()
    var videoInfo: VideoInfo = null
    var videoMeta = MetaData()
    var audioInfo: AudioInfo = null
    var audioMeta = MetaData()

    var mode: String = null

    def processLine(line: String) = {
      logger.debug(line)
      line match {
        case "Metadata:" if mode == null => mode = "general"
        case CreationTime(time) => mediaInfo = mediaInfo.copy(created = dateParser.parse(time).getTime)
        case DurationStartAndBitrate(hours, minutes, seconds, millis, start, bitRate) => {
          val duration = hours.toDouble.hours + minutes.toDouble.minutes + seconds.toDouble.seconds + (millis.toDouble / 100.0)
          mediaInfo = mediaInfo.copy(duration = duration, start = start.toDouble, bitRate = bitRate.toLong)
        }
        case DurationStartAndBitrateShort(hours, minutes, seconds, millis, bitRate) => {
          val duration = hours.toDouble.hours + minutes.toDouble.minutes + seconds.toDouble.seconds + (millis.toDouble / 100.0)
          mediaInfo = mediaInfo.copy(duration = duration, bitRate = bitRate.toLong)
        }
        case VideoDetails(codec, colorSpace, width, height, sizeExtra, kbPerSecond, fps) => {
          videoInfo = VideoInfo(codec, width.toInt, height.toInt, fps.toDouble)
          mode = "video"
        }
        case AudioDetails(codec, extra, range, speakers) => {
          val stereo = speakers match {
            case "stereo" => true
            case "2 channels" => true
            case "5.1" => true
            case "5.1(side)" => true
            case _ => throw new RuntimeException(s"Unsupported speaker configuration: $speakers.")
          }
          audioInfo = AudioInfo(codec, range.toLong, stereo)
          mode = "audio"
        }
        case MetaDataRegex(key, value) => mode match {
          case null => // Ignore
          case "general" => mediaMeta = mediaMeta(key, value)
          case "video" => videoMeta = videoMeta(key, value)
          case "audio" => audioMeta = audioMeta(key, value)
        }
        case _ => // Ignore
      }
    }

    val log: String => Unit = (s: String) => processLine(s.trim)
    val command = Seq("ffprobe", filename)
    val result = command ! ProcessLogger(log, log)
    if (result != 0) {
      throw new RuntimeException(s"Bad result while trying to execute ffprobe: $result. Command: ${command.mkString(" ")}. Verify ffmpeg is installed.")
    }

    if (videoInfo != null) {
      videoInfo = videoInfo.copy(meta = videoMeta)
    }
    if (audioInfo != null) {
      audioInfo = audioInfo.copy(meta = audioMeta)
    }
    mediaInfo.copy(meta = mediaMeta, video = videoInfo, audio = audioInfo)
  }

  def screenGrab(video: File, offset: Double, image: File) = {
    val transcode = Transcode(video, image, start = offset, videoCodec = VideoCodec.mjpeg, videoFrames = 1, disableAudio = true, forceFormat = "rawvideo")
    transcode.execute()
  }

  def scaleAndCropFilters(info: MediaInfo, width: Int, height: Int) = {
    val widthAspect = width.toDouble / info.video.width.toDouble
    val heightAspect = height.toDouble / info.video.height.toDouble
    val (scaledWidth, scaledHeight) = if (widthAspect > heightAspect) {
      width -> math.round(info.video.height.toDouble * widthAspect).toInt
    } else {
      math.round(info.video.width.toDouble * heightAspect).toInt -> height
    }
    val xOffset = math.round((scaledWidth - width) / 2.0).toInt
    val yOffset = math.round((scaledHeight - height) / 2.0).toInt
    ScaleFilter(scaledWidth, scaledHeight) -> CropFilter(width, height, xOffset, yOffset)
  }

  def webH264Transcoder(input: File, output: File) = {
    Transcode(
      input, output, videoCodec = VideoCodec.libx264, videoProfile = VideoProfile.High, preset = Preset.Slow,
      videoBitRate = 500000, maxRate = 500000, bufferSize = 1000000, threads = 0, audioCodec = AudioCodec.libfdkAAC,
      audioBitRate = 128000
    )
  }
  def webmTranscoder(input: File, output: File) = {
    Transcode(
      input, output, videoCodec = VideoCodec.libvpx, videoBitRate = 500000, maxRate = 500000, audioCodec = AudioCodec.libvorbis
    )
  }

  def webAudioTranscoder(input: File, output: File) = {
    Transcode(
      input, output, audioCodec = AudioCodec.libmp3Lame, audioQuality = 2
    )
  }

  def copy(input: File, output: File, start: Option[Double] = None, duration: Option[Double] = None) = {
    var t = Transcode(
      input, output, videoCodec = VideoCodec.copy, audioCodec = AudioCodec.copy
    )
    start match {
      case Some(s) => t = t.copy(start = s)
      case None => // Ignore
    }
    duration match {
      case Some(d) => t = t.copy(duration = d)
      case None => // Ignore
    }
    t
  }
}