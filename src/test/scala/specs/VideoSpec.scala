package specs

import java.io.File

import org.matthicks.media4s.video.VideoUtil
import org.matthicks.media4s.video.info.MediaInfo
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

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
  }
}
