package org.matthicks.media4s.video

import java.io.File

import com.outr.scribe.Logging
import org.matthicks.media4s.video.codec.{AudioCodec, VideoCodec}
import org.matthicks.media4s.video.filter.{CropFilter, ScaleFilter}
import org.matthicks.media4s.video.info.MediaInfo
import org.matthicks.media4s.video.transcode.Transcode

import scala.sys.process._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object VideoUtil extends Logging {
  /**
    * Gets the MediaInfo for the specified media file.
    *
    * @param file an audio or video file to get the information for.
    * @return MediaInfo
    */
  def info(file: File): MediaInfo = {
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
    val result = command ! ProcessLogger(log, (err: String) => logger.error(err))
    if (result != 0) {
      throw new RuntimeException(s"Bad result while trying to execute ffprobe: $result. Command: ${command.mkString(" ")}. Verify ffmpeg is installed.")
    }
    MediaInfo(b.toString())
  }

  /**
    * Create an image from a specific position in a video file.
    *
    * @param video the video to grab from
    * @param offset the position in the video
    * @param image the file to write out the image to
    * @return Transcode instance that can be executed
    */
  def screenGrab(video: File, offset: Double, image: File): Transcode = {
    Transcode(video, image, start = offset, videoCodec = VideoCodec.mjpeg, videoFrames = 1, disableAudio = true, forceFormat = "rawvideo")
  }

  /**
    * Creates ScaleFilter and CropFilter to size the media to the specified width and height.
    *
    * @param info the info for the media that needs to be sized
    * @param width the destination width
    * @param height the destination height
    * @return (ScaleFilter, CropFilter)
    */
  def scaleAndCropFilters(info: MediaInfo, width: Int, height: Int): (ScaleFilter, CropFilter) = {
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

  /**
    * Preset for Web H264 video transcoding.
    *
    * @param input the source video file
    * @param output the destination video file
    * @return Transcode instance that can be executed
    */
  def webH264Transcoder(input: File, output: File): Transcode = Transcode(
    input, output, videoCodec = VideoCodec.libx264, videoProfile = VideoProfile.High, preset = Preset.Slow,
    videoBitRate = 500000, maxRate = 500000, bufferSize = 1000000, threads = 0, audioCodec = AudioCodec.AAC,
    audioBitRate = 128000
  )

  /**
    * Preset for WebM video transcoding.
    *
    * @param input the source video file
    * @param output the destination video file
    * @param vp9 true if encode with VP9, false to use vp8 (defaults to true)
    * @return Transcode instance that can be executed
    */
  def webmTranscoder(input: File, output: File, vp9: Boolean = true): Transcode = Transcode(
    input, output,
    videoCodec = if (vp9) VideoCodec.libvpx_vp9 else VideoCodec.libvpx,
    videoBitRate = 500000,
    maxRate = 500000,
    audioCodec = AudioCodec.libvorbis
  )

  /**
    * Preset for MP3 audio transcoding.
    *
    * @param input the source audio file
    * @param output the destination audio file
    * @return Transcode instance that can be executed
    */
  def webAudioTranscoder(input: File, output: File): Transcode = Transcode(
    input, output, audioCodec = AudioCodec.libmp3Lame, audioQuality = 2
  )

  /**
    * Creates a copy of the audio or video allowing to limit the start and duration of the resulting file.
    *
    * @param input the source video or audio file
    * @param output the destination video or audio file
    * @param start the position (in seconds) to start copying from the source (defaults to None)
    * @param duration the amount of time (in seconds) to copy from start (defaults to None)
    * @return Transcode instance that can be executed
    */
  def copy(input: File, output: File, start: Option[Double] = None, duration: Option[Double] = None): Transcode = {
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