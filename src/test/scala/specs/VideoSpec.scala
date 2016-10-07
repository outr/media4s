package specs

import java.io.File

import org.matthicks.media4s.video.VideoUtil
import org.matthicks.media4s.video.transcode.{FFMPEGTranscoder, TranscodeListener}
import org.scalatest.{Matchers, WordSpec}

class VideoSpec extends WordSpec with Matchers {
  "VideoUtil" should {
    val trailer480p = new File("content/video/trailer_480p.mov")

    "gather the correct information for trailer_480p.mov" in {
      val info = VideoUtil.info(trailer480p)
      info.duration should be(32.995)
      info.start should be(0.0)
      info.bitRate should be(2681863)
      info.video.width should be(853)
      info.video.height should be(480)
      info.video.fps should be(25.0)
      info.audio.bitRate should be(428605)
    }
    "transcode quickly to an MP4 video" in {
      val output = File.createTempFile("test", ".mp4")
      var previous: Double = 0.0
      val listener = new TranscodeListener {
        override def log(message: String): Unit = {}

        override def progress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean): Unit = {
          percentage should be >= previous
          println(s"progress: $percentage")
          previous = percentage
        }
      }
      try {
        val info = VideoUtil.info(trailer480p)
        val t = FFMPEGTranscoder()
          .input(trailer480p)
          .webH264()
          .scaleAndCrop(info.video.width, info.video.height, 50, 50)
          .output(output)
        t.execute(Some(listener))
        math.floor(previous) should be(1.0)
      } finally {
        if (!output.delete()) {
          output.deleteOnExit()
        }
      }
    }
    "transcode five second clip quickly to an MP4 video" in {
      val output = File.createTempFile("test", ".mp4")
      var previous: Double = 0.0
      val listener = new TranscodeListener {
        override def log(message: String): Unit = {}

        override def progress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean): Unit = {
          percentage should be >= previous
          println(s"progress: $percentage")
          previous = percentage
        }
      }
      try {
        val info = VideoUtil.info(trailer480p)
        val t = FFMPEGTranscoder()
          .input(trailer480p)
          .webH264()
          .scaleAndCrop(info.video.width, info.video.height, 50, 50)
          .duration(25.0)
          .output(output)
        t.execute(Some(listener))
        math.floor(previous) should be(1.0)
      } finally {
        if (!output.delete()) {
          output.deleteOnExit()
        }
      }
    }
  }
}
