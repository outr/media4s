package org.matthicks.media4s.video

import fabric.io._
import fabric.rw.Asable

import java.io.File
import org.matthicks.media4s.video.info.MediaInfo
import org.matthicks.media4s.video.transcode.FFMPEGTranscoder

import scala.sys.process._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object VideoUtil {
  /**
    * Gets the MediaInfo for the specified media file.
    *
    * @param file an audio or video file to get the information for.
    * @return MediaInfo
    */
  def info(file: File, logger: String => Unit = println): MediaInfo = {
    assert(file.isFile, s"File was not found: ${file.getAbsolutePath}")
    val command = Seq(
      "ffprobe",
      "-v",
      "quiet",
      "-print_format", "json",
      "-show_format",
      "-show_streams",
      file.getCanonicalPath
    )
    val b = new StringBuilder
    val log: String => Unit = (line: String) => {
      b.append(line)
      b.append('\n')
      ()
    }
    val result = command ! ProcessLogger(log, logger)
    if (result != 0) {
      throw new RuntimeException(s"Bad result while trying to execute ffprobe: $result. Command: ${command.mkString(" ")}. Verify ffmpeg is installed.")
    }
    JsonParser(b.toString()).as[MediaInfo]
  }

  /**
    * Creates a copy of the audio or video allowing to limit the start and duration of the resulting file.
    *
    * @param input the source video or audio file
    * @param output the destination video or audio file
    * @param start the position (in seconds) to start copying from the source (defaults to None)
    * @param duration the amount of time (in seconds) to copy from start (defaults to None)
    * @return Transcode instance that can be executed
    */
  def copy(input: File, output: File, start: Option[Double] = None, duration: Option[Double] = None): FFMPEGTranscoder = {
    var t = FFMPEGTranscoder().input(input).codec.copy()
    start match {
      case Some(s) => t = t.start(s)
      case None => // Ignore
    }
    duration match {
      case Some(d) => t = t.duration(d)
      case None => // Ignore
    }
    t
  }
}