package org.matthicks.media4s.video

case class TranscodeProgress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean)
