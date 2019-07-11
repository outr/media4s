package specs

import java.io._

import io.youi.Color
import org.matthicks.media4s.image.{ImageInfo, ImageUtil, SVGTools}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.Matchers

class ImageSpec extends AnyWordSpec with Matchers {
  info("scaling up")
  import ImageUtil.scaleUp
  val baseInfo: ImageInfo = ImageInfo(0, 0, 0, null, None, None)

  "bad dimension" in {
    baseInfo should equal(scaleUp(baseInfo, 0, 0))
  }
  "correctly sized" in {
    val nopInfo = baseInfo.copy(width = 400, height = 400)
    nopInfo should equal(scaleUp(nopInfo, 400, 400))
  }
  "both dimensions plenty big" in {
    val nopInfo = baseInfo.copy(width = 800, height = 400)
    nopInfo should equal(scaleUp(nopInfo, 400, 200))
  }
  "width too small" in {
    val sWinfo = baseInfo.copy(200, 100)
    scaleUp(sWinfo, 400, 200) should equal(sWinfo.copy(400, 200))
  }
  "width too small, square" in {
    val sWinfo = baseInfo.copy(60, 60)
    scaleUp(sWinfo, 400, 200) should equal(sWinfo.copy(400, 400))
  }
  "height too small" in {
    val sHinfo = baseInfo.copy(400, 100)
    scaleUp(sHinfo, 400, 200) should equal(sHinfo.copy(800, 200))
  }
  "add watermark" in {
    val input = new File("content/png/imagecontent.jpg")
    val overlay = "content/png/watermark-100x100.png"
    val watermarked = new File("/tmp/watermarked.png")
    if (watermarked.exists()) watermarked.delete()
    ImageUtil.addWatermark(input, watermarked, overlay)
    watermarked.exists() should equal (true)
  }
  "add watermark (2)" in {
    val input = new File("content/png/imagecontent.jpg")
    val overlay = "content/png/watermark-100x100.png"
    val watermarked = new File("/tmp/watermarked.jpg")
    if (watermarked.exists()) watermarked.delete()
    ImageUtil.addWatermark(input, watermarked, overlay)
    watermarked.exists() should equal (true)
  }
  "resize" in {
    val input = new File("content/png/imagecontent.jpg")
    val output = new File("/tmp/resized.png")
    ImageUtil.generateResized(input, output, Some(50))
    output.exists() should equal (true)
  }
  "resize (2)" in {
    val input = new File("content/png/imagecontent.jpg")
    val output = new File("/tmp/resized2.jpg")
    ImageUtil.generateResized(input, output, Some(50))
    output.exists() should equal (true)
  }
  "resize SVG" in {
    val input = new File("content/svg/dolphin.svg")
    val output = new File("/tmp/resized3.png")
    ImageUtil.generateResized(input, output, width = Some(100))
    output.exists() should equal(true)
    val info = ImageUtil.info(output)
    info.width should be(100)
    info.height should be(100)
  }
  "colorize an SVG" in {
    val input = new File("content/svg/sheep.svg")
    val output = new File("/tmp/blue-sheep.svg")
    SVGTools.colorize(input, Color.Blue, output)
  }
}
