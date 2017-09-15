package org.matthicks.media4s.image

import java.io._
import java.util.Base64

import org.im4java.core.{CompositeCmd, ConvertCmd, IMOperation, IMOps, Info}
import org.matthicks.media4s.Size
import org.powerscala.io._

object ImageUtil {
  var iccProfiles = "/opt/icc"

  def info(file: File): ImageInfo = {
    val filename = file.getAbsolutePath
    val info = new Info(filename)
    val extension = filename.substring(filename.lastIndexOf('.') + 1)
    val imageType = ImageType.fromExtension(extension)

    ImageInfo(
      width = info.getImageWidth(0),
      height = info.getImageHeight(0),
      depth = info.getImageDepth(0),
      format = info.getImageFormat(0),
      imageType = imageType,
      colorSpace = Option(info.getProperty("Colorspace"))
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

  /**
   * Resizes the supplied file with adaptive resizing based on the supplied
   * width and/or height values.
   *
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
                      quality: Double = 0.0,
                      isCMYK: Boolean = false): Unit = {
    val original = input.getAbsolutePath
    val altered = output.getAbsolutePath
    val op = new IMOperation
    op.autoOrient()

    if (altered.endsWith(".jpg")) {
      // Remove transparent background
      op.flatten()
    }

    if (strip) op.strip()
    if (gaussianBlur != 0.0) op.gaussianBlur(gaussianBlur)
    if (quality != 0) op.quality(quality)

    op.density(288)
    op.addImage(s"$original[0]")

    // Only resize to shrink image to max width/height where defined.
    op.adaptiveResize(
      width.map(Int.box).orNull,
      height.map(Int.box).orNull,
      '>')

    if (isCMYK) applyCMYKConversion(op)

    op.addImage(altered)

    val cmd = new ConvertCmd
    cmd.run(op)
  }

  def applyCMYKConversion(op: IMOperation): IMOps = {
    if (!new File(s"$iccProfiles/CMYK/USWebCoatedSWOP.icc").exists()) {
      throw new RuntimeException(s"ICC Profiles not installed properly in $iccProfiles.")
    }
    op.profile(s"$iccProfiles/CMYK/USWebCoatedSWOP.icc")
    op.profile(s"$iccProfiles/RGB/AdobeRGB1998.icc")
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
                        height: Int,
                        isCMYK: Boolean = false): Unit = {
    val original = input.getAbsolutePath
    val altered = output.getAbsolutePath
    val op = new IMOperation
    op.autoOrient()

    op.density(288)
    op.addImage(s"$original[0]")
    op.thumbnail(width, height)
    op.background("transparent")
    op.flatten()
    op.gravity("center")
    op.extent(width, height)
    if (isCMYK) applyCMYKConversion(op)
    op.addImage(altered)

    val cmd = new ConvertCmd
    cmd.run(op)
  }

  def pngToJpg(input: File, output: File): Unit = {
    val op = new IMOperation
    op.autoOrient()
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

  def generateGIFCropped(input: File,
                         output: File,
                         width: Int,
                         height: Int): Unit = {
    val info = this.info(input)
    var transcoder = GIFSicleTranscoder(input, output)
      .resize(Some(width), Some(height))
      .optimize(3)
    val destination = Size(width, height)
    if (info.aspectRatio != destination.aspectRatio) {
      val cropped = info.cropToAspectRatio(destination)
      val x1 = math.round((info.width - cropped.width) / 2.0).toInt
      val y1 = math.round((info.height - cropped.height) / 2.0).toInt
      val x2 = x1 + cropped.width
      val y2 = y1 + cropped.height
      transcoder = transcoder.crop(x1, y1, x2, y2)
    }
    transcoder.execute()
  }

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
                      quality: Double = 0.0d,
                      flatten: Boolean = true,
                      isCMYK: Boolean = false): Unit = {
    val original = input.getAbsolutePath
    val altered = output.getAbsolutePath
    val op = new IMOperation
    op.autoOrient()

    if (outputType == ImageType.GIF || outputType == ImageType.PNG) {
      op.background("none")
    }

    if (outputType == ImageType.JPEG) {
      if (strip) op.strip()
      if (gaussianBlur != 0.0) op.gaussianBlur(gaussianBlur)
      if (quality != 0) op.quality(quality)
    }

    op.density(288)
    op.addImage(s"$original[0]")
    if (flatten) op.flatten()
    op.resize(width, height, '^')
    op.gravity("center")
    op.crop(width, height, 0, 0)
    op.p_repage()
    if (isCMYK) applyCMYKConversion(op)
    op.addImage(altered)

    val cmd = new ConvertCmd
    cmd.run(op)
  }

  /**
    * Takes a base64 encoded String and outputs it to the file specified as a proper binary representation.
    *
    * @param base64 the base64 encoded image
    * @param file the binary file to output to
    */
  def saveBase64(base64: String, file: File): Unit = {
    val index = base64.indexOf("base64,")
    val encoded = if (index != -1) {
      base64.substring(index + 7)
    } else {
      base64
    }
    val decoded = Base64.getDecoder.decode(encoded)
    IO.stream(new ByteArrayInputStream(decoded), file)
    ()
  }
}
