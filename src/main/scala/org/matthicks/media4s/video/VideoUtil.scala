package org.matthicks.media4s.video

import java.io.File
import java.text.SimpleDateFormat

import com.outr.scribe.Logging
import org.powerscala.concurrent.{Elapsed, Time}
import org.powerscala.concurrent.Time._

import scala.collection.mutable.ListBuffer
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

/**
 * @param inputDelay  Delays the input file's video streams by [[inputDelay]] seconds
 * @param start  Advances the input file's video streams by [[start]] seconds
 */
case class Transcode(input: File,
                     output: File,
                     inputDelay: Double = 0.0,
                     videoFrames: Int = -1,
                     videoCodec: VideoCodec = null,
                     videoProfile: VideoProfile = null,
                     preset: Preset = null,
                     videoBitRate: Long = -1L,
                     maxRate: Long = -1L,
                     bufferSize: Long = -1L,
                     threads: Int = -1,
                     audioCodec: AudioCodec = null,
                     audioBitRate: Long = -1L,
                     audioQuality: Int = -1,
                     pass: Int = -1,
                     forceFormat: String = null,
                     disableAudio: Boolean = false,
                     overwrite: Boolean = true,
                     videoFilters: List[VideoFilter] = Nil,
                     start: Double = 0.0,
                     duration: Double = 0.0,
                     priority: Int = 10) extends Logging {
  private val ProgressRegex = """frame=\s*(\d+) fps=\s*([0-9.]+) q=([-0-9.]+) (L?)size=\s*(\d+)kB time=(\d{2}):(\d{2}):(\d{2})[.](\d+) bitrate=\s*([0-9.]+)kbits/s.*""".r

  lazy val info = VideoUtil.info(input)

  lazy val command = {
    val command = ListBuffer.empty[String]
    command += "nice"
    command += s"--adjustment=$priority"
    command += "ffmpeg"
    def add(args: Any*) = args.foreach {
      case arg => command += arg.toString
    }
    if (inputDelay > 0.0) {
      add("-itsoffset", math.round(inputDelay))
    }
    add("-i", input.getAbsolutePath)
    if (videoCodec != null) {
      add("-codec:v", videoCodec.value)
    }
    if (videoFrames != -1) {
      add("-vframes", videoFrames)
    }
    if (videoProfile != null) {
      add("-profile:v", videoProfile.value)
    }
    if (preset != null) {
      add("-preset", preset.value)
    }
    if (videoBitRate != -1L) {
      add("-b:v", videoBitRate)
    }
    if (maxRate != -1L) {
      add("-maxrate", maxRate)
    }
    if (bufferSize != -1L) {
      add("-bufsize", bufferSize)
    }
    if (threads != -1) {
      add("-threads", threads)
    }
    if (audioCodec != null) {
      add("-codec:a", audioCodec.value)
    }
    if (audioBitRate != -1L) {
      add("-b:a", audioBitRate)
    }
    if (audioQuality != -1) {
      add("-qscale:a", audioQuality)
    }
    if (pass != -1) {
      add("-pass", pass)
    }
    if (disableAudio) {
      add("-an")
    }
    if (forceFormat != null) {
      add("-f", forceFormat)
    }
    if (videoFilters.nonEmpty) {
      add("-vf", videoFilters.map(vf => vf.value).mkString(","))
    }
    if (start > 0.0) {
      val e = Elapsed(start)
      add("-ss", f"${e.hours}%02d:${e.minutes}%02d:${e.seconds}%02d")
    }
    if (duration > 0.0) {
      val e = Elapsed(duration)
      add("-t", f"${e.hours}%02d:${e.minutes}%02d:${e.seconds}%02d")
    }
    add(output.getAbsolutePath)
    if (overwrite) {
      add("-y")
    } else {
      add("-n")
    }
    command.toSeq
  }

  def execute(monitor: TranscodeProgress => Unit = null) = {
    logger.debug(command.mkString(" "))
    val start = System.currentTimeMillis()
    var lastLine: String = null
    val log = (line: String) => line match {
      case ProgressRegex(frame, fps, q, last, size, hours, minutes, seconds, millis, bitRate) => {
        val percentage = frame.toDouble / info.frames.toDouble
        val time = hours.toDouble.hours + minutes.toDouble.minutes + seconds.toDouble.seconds + (millis.toDouble / 100.0)
        val elapsed = Time.fromMillis(System.currentTimeMillis() - start)
        val finished = last == "L"
        val p = TranscodeProgress(percentage, frame.toInt, fps.toDouble, q.toDouble, math.round(size.toDouble * 1000), time, math.round(bitRate.toDouble * 1000), elapsed, finished)
        if (monitor != null) {
          monitor(p)
        }
      }
      case _ => {
        logger.debug(line)
        lastLine = line
      }
    }
    val result = command ! ProcessLogger(log)
    if (result != 0) {
      throw new TranscodeFailedException(s"Failed transcoding (${command.mkString(" ")}). Received result: $result. $lastLine")
    }
  }
}

class TranscodeFailedException(message: String) extends RuntimeException(message)

case class TranscodeProgress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean)

trait VideoFilter {
  def value: String
}

case class ScaleFilter(width: Int, height: Int) extends VideoFilter {
  val value = s"scale=$width:$height"
}

case class CropFilter(width: Int, height: Int, x: Int = 0, y: Int = 0) extends VideoFilter {
  val value = s"crop=$width:$height:$x:$y"
}

sealed abstract class VideoCodec(val value: String)

object VideoCodec {
  case object copy extends VideoCodec("copy")
  case object libx264 extends VideoCodec("libx264")
  case object libvpx extends VideoCodec("libvpx")
  case object mjpeg extends VideoCodec("mjpeg")

  val values = Vector(copy, libx264, libvpx, mjpeg)
}

sealed abstract class VideoProfile(val value: String)

object VideoProfile {
  case object Baseline extends VideoProfile("baseline")
  case object Main extends VideoProfile("main")
  case object High extends VideoProfile("high")

  val values = Vector(Baseline, Main, High)
}

sealed abstract class Preset(val value: String)

object Preset {
  case object UltraFast extends Preset("ultrafast")
  case object SuperFast extends Preset("superfast")
  case object VeryFast extends Preset("veryfast")
  case object Faster extends Preset("faster")
  case object Fast extends Preset("fast")
  case object Medium extends Preset("medium")
  case object Slow extends Preset("slow")
  case object Slower extends Preset("slower")
  case object VerySlow extends Preset("veryslow")
  case object Placebo extends Preset("placebo")

  val values = Vector(UltraFast, SuperFast, VeryFast, Faster, Fast, Medium, Slow, Slower, VerySlow, Placebo)
}

sealed abstract class AudioCodec(val value: String)

object AudioCodec {
  case object copy extends AudioCodec("copy")
  case object libfdkAAC extends AudioCodec("libfdk_aac")
  case object libmp3Lame extends AudioCodec("libmp3lame")
  case object libvorbis extends AudioCodec("libvorbis")

  val values = Vector(copy, libfdkAAC, libmp3Lame, libvorbis)
}