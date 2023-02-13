package specs

import java.io.File
import org.matthicks.media4s.video.VideoUtil
import org.matthicks.media4s.video.filter.{CropFilter, ScaleFilter}
import org.matthicks.media4s.video.transcode.{FFMPEGTranscoder, TranscodeListener}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import Ordering.Double.TotalOrdering

class VideoSpec extends AnyWordSpec with Matchers {
  "VideoUtil" should {
    val trailer480p = new File("content/video/trailer_480p.mov")

    val testVideoWithDataStream = new File("content/video/test_3.mov")

    "gather the correct information for trailer_480p.mov" in {
      val info = VideoUtil.info(trailer480p)
      info.duration should be(32.995)
      info.start should be(0.0)
      info.bitRate should be(2681863)
      info.video.width should be(853)
      info.video.height should be(480)
      info.video.fps should be(25.0)
      info.audio.get.bitRate should be(428605)
    }
    "gather the correct information and ignore data codec_type for test_3.mov" in {
      val info = VideoUtil.info(testVideoWithDataStream)
      info.video.width should be(1920)
      info.video.height should be(1080)
      info.video.fps should be(60000)
      info.audio.isEmpty should be(true)
      println(info)
    }

    "transcode quickly to an MP4 video" in {
      val output = File.createTempFile("test", ".mp4")
      var previous: Double = 0.0
      val listener = new TranscodeListener {
        override def log(message: String): Unit = {}

        override def progress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean): Unit = {
          percentage should be >= previous
          previous = percentage
        }
      }
      try {
        val info = VideoUtil.info(trailer480p)
        val t = FFMPEGTranscoder()
          .input(trailer480p)
          .webH264()
          .videoFilters(ScaleFilter.create(info, 50, 50), CropFilter.create(info, 50, 50))
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
          previous = percentage
        }
      }
      try {
        val info = VideoUtil.info(trailer480p)
        val t = FFMPEGTranscoder()
          .input(trailer480p)
          .webH264()
          .videoFilters(ScaleFilter.create(info, 50, 50), CropFilter.create(info, 50, 50))
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
    // TODO: investigate this further
    /*"transcode multiple outputs for one video" in {
      val output1 = File.createTempFile("test", ".mp4")
      val output2 = File.createTempFile("test", ".webm")
      var previous: Double = 0.0
      val listener = new TranscodeListener {
        override def log(message: String): Unit = {}

        override def progress(percentage: Double, frame: Int, fps: Double, q: Double, size: Long, time: Double, bitRate: Long, elapsed: Double, finished: Boolean): Unit = {
          percentage should be >= previous
          previous = percentage
        }
      }
      try {
        val info = VideoUtil.info(trailer480p)
        val t = FFMPEGTranscoder()
          .input(trailer480p)
          .webH264()
          .scaleAndCrop(info.video.width, info.video.height, 50, 50)
          .output(output1)
          .webm()
          .scaleAndCrop(info.video.width, info.video.height, 50, 50)
          .output(output2)
        t.execute(Some(listener))
        math.floor(previous) should be(1.0)

        val o1Info = VideoUtil.info(output1)
        o1Info.video.width should be(50)
        o1Info.video.height should be(50)
        o1Info.video.codec should be("")

        val o2Info = VideoUtil.info(output2)
        o2Info.video.width should be(50)
        o2Info.video.height should be(50)
        o2Info.video.codec should be("")
      } finally {
        if (!output1.delete()) {
          output1.deleteOnExit()
        }
        if (!output2.delete()) {
          output2.deleteOnExit()
        }
      }
    }*/

    val sampleflv = new File("content/video/SampleVideo_320x240_1mb.flv")

    "gather the correct information for SampleVideo_320x240_1mb.flv" in {
      val info = VideoUtil.info(sampleflv)
      info.duration should be(10.64)
      info.start should be(0.0)
      info.bitRate should be(792900)
      info.video.width should be(320)
      info.video.height should be(240)
      info.video.fps should be(25.0)
      info.audio.get.bitRate should be(384000L)
    }

    val samplemkv = new File("content/video/SampleVideo_320x240_1mb.mkv")

    "gather the correct information for SampleVideo_320x240_1mb.mkv" in {
      val info = VideoUtil.info(samplemkv)
      info.duration should be(9.818)
      info.start should be(0.0)
      info.bitRate should be(860233)
      info.video.width should be(320)
      info.video.height should be(240)
      info.video.fps should be(25.0)
      info.audio.get.bitRate should be(0)
    }
  }
}
