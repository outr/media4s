package specs

import org.matthicks.media4s.Size
import org.scalatest.{Matchers, WordSpec}

class SizeSpecs extends WordSpec with Matchers {
  "Size" when {
    "cropping to aspect ratio" should {
      "calculate proper crop dimensions for aspect ratio (800x650)" in {
        val size = Size(800, 650)
        val cropped = size.cropToAspectRatio(Size(1024, 768))
        cropped.width should be(800)
        cropped.height should be(600)
      }
      "calculate proper crop dimensions for aspect ratio (800x300)" in {
        val size = Size(800, 300)
        val cropped = size.cropToAspectRatio(Size(1024, 768))
        cropped.width should be(400)
        cropped.height should be(300)
      }
      "calculate proper crop dimensions for aspect ratio (1024x900)" in {
        val size = Size(1024, 900)
        val cropped = size.cropToAspectRatio(Size(800, 600))
        cropped.width should be(1024)
        cropped.height should be(768)
      }
      "calculate proper crop dimensions for aspect ratio (1024x500)" in {
        val size = Size(1024, 500)
        val cropped = size.cropToAspectRatio(Size(800, 600))
        cropped.width should be(667)
        cropped.height should be(500)
      }
    }
    "scaling up" should {
      "calculate the proper dimensions for 1024x768" in {
        val size = Size(800, 300)
        val scaled = size.scaleUp(Some(1024), Some(768))
        scaled.width should be(2048)
        scaled.height should be(768)
        scaled.aspectRatio should be(size.aspectRatio)
      }
      "calculate the proper dimensions for width of 1024" in {
        val size = Size(800, 300)
        val scaled = size.scaleUp(Some(1024), None)
        scaled.width should be(1024)
        scaled.height should be(384)
        scaled.aspectRatio should be(size.aspectRatio)
      }
      "calculate the proper dimensions for width of 719" in {
        val size = Size(800, 300)
        val scaled = size.scaleUp(Some(719), None)
        scaled.width should be(800)
        scaled.height should be(300)
        scaled.aspectRatio should be(size.aspectRatio)
      }
    }
  }
}
