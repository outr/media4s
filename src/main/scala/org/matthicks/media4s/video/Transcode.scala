package org.matthicks.media4s.video

import java.io.File

import com.outr.scribe.Logging
import org.powerscala.concurrent.{Elapsed, Time}

import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger

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
