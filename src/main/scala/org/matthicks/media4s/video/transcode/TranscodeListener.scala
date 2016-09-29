package org.matthicks.media4s.video.transcode

trait TranscodeListener {
  def progress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean): Unit

  def log(message: String): Unit
}
