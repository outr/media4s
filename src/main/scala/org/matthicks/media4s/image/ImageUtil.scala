package org.matthicks.media4s.image

import java.io._

import com.outr.scribe.Logging
import org.apache.batik.transcoder.svg2svg.SVGTranscoder
import org.apache.batik.transcoder._
import org.apache.batik.transcoder.image.{ImageTranscoder, JPEGTranscoder, PNGTranscoder}
import org.im4java.core.{CompositeCmd, ConvertCmd, IMOperation, Info}
import org.matthicks.media4s.file.FileHelpers

import scala.util.{Success, Try}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ImageUtil extends Logging {
  def info(file: File): ImageInfo = {
    val filename = file.getAbsolutePath
    val info = new Info(filename)
    val extension = filename.substring(filename.lastIndexOf('.') + 1)
    val imageType = ImageType.fromExtension(extension).getOrElse(
      throw new RuntimeException(s"No ImageType defined for extension: $extension"))

    ImageInfo(
      width = info.getImageWidth,
      height = info.getImageHeight,
      depth = info.getImageDepth,
      format = info.getImageFormat,
      imageType = imageType
    )
  }

  def scaleUp(imageInfo: ImageInfo, minWidth: Int, minHeight: Int): ImageInfo =
    if (imageInfo.width < minWidth || imageInfo.height <= minHeight) {
      val widthScaled = imageInfo.copy(width = minWidth, height =
        math.round(minWidth * imageInfo.aspectRatio).toInt)

      val heightScaled = imageInfo.copy(
        width = math.round(minHeight / imageInfo.aspectRatio).toInt,
        height = minHeight
      )

      if (widthScaled.pixels > heightScaled.pixels) widthScaled
      else heightScaled
    } else {
      imageInfo
    }

  def isRasterImage(file: File): Boolean =
    info(file).imageType != ImageType.SVG

  /**
   * Validate a Vector
   *
   * Since it is possible to have a valid Vector with some limitations that a
   * user may need to know, the [[Success]] returned will carry such warnings if
   * applicable and available.
   */
  def validateVector(file: File): Try[List[String]] =
    Try {
      val srcInfo = info(file)

      val tempFile = FileHelpers.createTempFile("png")
      try {
        rasterizeVectorGraphic(new PNGTranscoder(), file, tempFile,
          Some(srcInfo.width), Some(srcInfo.height))
      } finally {
        FileHelpers.deleteFile(tempFile)
      }
    }.map(_ => List.empty[String]) // Would need a custom ErrorHandler to capture the Warnings.

  /**
   * Rasterize an SVG image to image type based on provided transcoder
   *
   * @param transcoder {{ImageTranscoder} used to rasterize the image.
   * @param inFile source
   * @param outFile target image in raster format
   * @param width for destination image
   * @param height for destination image
   */
  def rasterizeVectorGraphic(transcoder: ImageTranscoder,
                             inFile: File,
                             outFile: File,
                             width: Option[Int] = None,
                             height: Option[Int] = None,
                             maxWidth: Option[Int] = None,
                             maxHeight: Option[Int] = None): Unit = {
    width.foreach(n =>
      transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, n.toFloat))
    height.foreach(n =>
      transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, n.toFloat))
    maxWidth.foreach(n =>
      transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_WIDTH, n.toFloat))
    maxHeight.foreach(n =>
      transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_HEIGHT, n.toFloat))

    val inputURL = inFile.toURI.toURL
    val reader = new BufferedReader(new InputStreamReader(inputURL.openStream()))
    val input = new TranscoderInput(reader)

    input.setURI(inputURL.toString)

    val outputStream = new BufferedOutputStream(new FileOutputStream(outFile))
    val output = new TranscoderOutput(outputStream)

    try transcoder.transcode(input, output)
    finally {
      reader.close()
      outputStream.close()
    }
  }

  /**
   * Format SVG image for web use
   */
  def formatVectorGraphic(inFile: File, outFile: File): Unit = {
    val transcoder = new SVGTranscoder()
    transcoder.addTranscodingHint(SVGTranscoder.KEY_FORMAT, true)

    val inputURL = inFile.toURI.toURL
    val reader = new BufferedReader(
      new InputStreamReader(inputURL.openStream()))

    val input = new TranscoderInput(reader)
    input.setURI(inputURL.toString)

    val writer = new BufferedWriter(
      new OutputStreamWriter(new FileOutputStream(outFile)))
    val output = new TranscoderOutput(writer)

    try transcoder.transcode(input, output)
    finally {
      reader.close()
      writer.close()
    }
  }

  /**
   * Convert ImageMagick style JPEG quality to Batik style JPEG quality
   *
   * Quality works differently in Batik v. ImageMagick
   * ImageMagick for JPEG 1 is the lowest quality and 100 is the highest
   * Batik for JPEG 0.1 is the lowest quality and 1.0 is the highest
   *
   * Because 0 is outside of the correct quality range for either, we will use
   * best quality when 0 is received.
   *
   * @param q ImageMagick style quality {{Double}}
   * @return Batik style quality {{Float}}
   */
  protected def toBatikQuality(q: Double): Float =
    if (q < 1) 1f else (q / 100).toFloat

  /**
   * Resizes the supplied file with adaptive resizing based on the supplied
   * width and/or height values.
   *
   * @note This method should not be used to alter a Vector image. It is called
   *       when an original image processed and and original vector image
   *       should not be have its size altered.
   * @param input the original file to resize
   * @param output generated image
   * @param width the width value option
   * @param height the height value option
   */
  def generateResized(input: File,
                      output: File,
                      width: Option[Int] = None,
                      height: Option[Int] = None,
                      strip: Boolean = true,
                      gaussianBlur: Double = 0.0,
                      quality: Double = 0.0): Unit =
    if (!isRasterImage(input)) formatVectorGraphic(input, output)
    else {
      val original = input.getAbsolutePath
      val altered = output.getAbsolutePath
      val op = new IMOperation

      if (altered.endsWith(".jpg")) {
        // Remove transparent background
        op.flatten()
      }

      if (strip) op.strip()
      if (gaussianBlur != 0.0) op.gaussianBlur(gaussianBlur)
      if (quality != 0) op.quality(quality)

      op.addImage(original)

      // Only resize to shrink image to max width/height where defined.
      op.adaptiveResize(
        width.map(Int.box).orNull,
        height.map(Int.box).orNull,
        '>')

      op.addImage(altered)

      val cmd = new ConvertCmd
      cmd.run(op)
    }

  /**
   * Generates a thumbnail of the specified image file at the supplied width and
   * height. The aspect ratio will be maintained and the outer extents will be
   * transparent in the generated PNG.
   *
   * @param input  Original image to generate a thumbnail from
   * @param output Generated image
   * @param width  Thumbnail width
   * @param height Thumbnail height
   */
  def generateThumbnail(input: File,
                        output: File,
                        width: Int,
                        height: Int): Unit =
    if (isRasterImage(input)) {
      val original = input.getAbsolutePath
      val altered = output.getAbsolutePath
      val op = new IMOperation

      op.addImage(original)
      op.thumbnail(width, height)
      op.background("transparent")
      op.gravity("center")
      op.extent(width, height)
      op.addImage(altered)

      val cmd = new ConvertCmd
      cmd.run(op)
    } else {
      /* 1. Rasterize SVG to PNG at original size.
       * 2. Convert the rasterized version of the source, a temp file, to
       *    thumbnail size using ImageMagick.
       */
      val rasterised = FileHelpers.createTempFile("png")
      try {
        val srcInfo = scaleUp(info(input), width, height)
        rasterizeVectorGraphic(new PNGTranscoder(), input, rasterised,
          Some(srcInfo.width), Some(srcInfo.height))
        generateThumbnail(rasterised, output, width, height)
      } finally {
        FileHelpers.deleteFile(rasterised)
      }
    }

  def pngToJpg(input: File, output: File): Unit = {
    val op = new IMOperation
    op.flatten()
    op.addImage(input.getAbsolutePath)
    op.addImage(output.getAbsolutePath)

    val cmd = new ConvertCmd
    cmd.run(op)
  }

  def addWatermark(input: File,
                   output: File,
                   overlayPath: String,
                   gravity: String = "center"): Unit = {
    val original = input.getAbsolutePath
    val outputPath = output.getAbsolutePath
    val altered =
      if (outputPath.endsWith(".jpg")) outputPath + ".png"
      else outputPath

    val op = new IMOperation

    op.compose("over")
    op.gravity(gravity)
    op.addImage(overlayPath)
    op.addImage(original)
    op.addImage(altered)

    val cmd = new CompositeCmd
    cmd.run(op)

    if (outputPath.endsWith(".jpg")) {
      val temp = new File(altered)
      pngToJpg(temp, new File(outputPath))
      if (!temp.delete()) {
        throw new RuntimeException(s"Unable to delete the temporary PNG: ${temp.getAbsolutePath}")
      }
    }
  }

  /** Given a gaussian blur and image quality parameter, determines if a JPEG or
    * PNG output is needed.
    */
  def destinationType(gaussianBlur: Double, quality: Double): ImageType =
    if (gaussianBlur == 0.0d && quality == 0.0d) ImageType.PNG
    else ImageType.JPEG

  /**
   * Generates an image completely filling the provided dimensions.
   *
   * A Vector image is not re-sized this way, but this method could be
   * called to produce a rasterized version of a vector graphic for use
   * as a library search results image.
   */
  def generateCropped(input: File,
                      output: File,
                      outputType: ImageType,
                      width: Int,
                      height: Int,
                      strip: Boolean = true,
                      gaussianBlur: Double = 0.0d,
                      quality: Double = 0.0d): Unit =
    if (isRasterImage(input)) {
      val original = input.getAbsolutePath
      val altered = output.getAbsolutePath
      val op = new IMOperation

      if (outputType == ImageType.JPEG) {
        if (strip) op.strip()
        if (gaussianBlur != 0.0) op.gaussianBlur(gaussianBlur)
        if (quality != 0) op.quality(quality)
      }

      op.addImage(original)
      op.resize(width, height, '^')
      op.gravity("center")
      op.crop(width, height, 0, 0)
      op.p_repage()
      op.addImage(altered)

      val cmd = new ConvertCmd
      cmd.run(op)
    } else {
      val srcInfo = scaleUp(info(input), width, height)
      val rasterised = FileHelpers.createTempFile(outputType.extension)

      try {
        if (outputType == ImageType.JPEG) {
          val transcoder = new JPEGTranscoder()
          // Highest quality
          transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 1f)
          rasterizeVectorGraphic(transcoder, input, rasterised,
            Some(srcInfo.width), Some(srcInfo.height))
          generateCropped(rasterised, output, outputType, width, height, strip,
            gaussianBlur, quality)
        } else {
          rasterizeVectorGraphic(new PNGTranscoder(), input, rasterised,
            Some(srcInfo.width), Some(srcInfo.height))
          generateCropped(rasterised, output, outputType, width, height)
        }
      } finally {
        FileHelpers.deleteFile(rasterised)
      }
    }
}
