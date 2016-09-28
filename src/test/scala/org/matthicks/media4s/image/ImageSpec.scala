package org.matthicks.media4s.image

import org.scalatest.{Matchers, WordSpec}

import java.io._

import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}

class ImageSpec extends WordSpec with Matchers {
  val testFileDirectory = "content/svg"
  val testFileName = s"$testFileDirectory/batikCandy.svg"
  "batik transcoder" in {
    // Verbose, but we want to see that batik's trancoder is working
    val transcoder = new JPEGTranscoder()
    val quality: Float = .8f
    transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, quality)

    val tempFileName = s"${testFileName.takeWhile(_ != '.')}.jpg"

    val tempFile = new File(tempFileName)

    try {
      val inputFile = new File(testFileName)
      val inputURL = inputFile.toURI.toURL
      val inputStream = inputURL.openStream()
      val reader = new BufferedReader(new InputStreamReader(inputStream))
      val input = new TranscoderInput(reader)

      input.setURI(inputURL.toString)

      val outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))
      val output = new TranscoderOutput(outputStream)

      try {
        transcoder.transcode(input, output)
      }
      finally {
        reader.close()
        outputStream.close()
      }
      val info = ImageUtil.info(tempFile)
      info.imageType should equal(ImageType.JPEG)
    }
    finally if (!tempFile.delete()) tempFile.deleteOnExit()
  }

  info("scaling up")
  import ImageUtil.scaleUp
  val baseInfo: ImageInfo = ImageInfo(0, 0, 0, null, null)

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
}
