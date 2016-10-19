package org.matthicks.media4s.video.filter

import org.matthicks.media4s.video.info.MediaInfo

case class CropFilter(width: Int, height: Int, x: Int = 0, y: Int = 0) extends VideoFilter {
  val value = s"crop=$width:$height:$x:$y"
}

object CropFilter {
  def create(originalWidth: Int, originalHeight: Int, destinationWidth: Int, destinationHeight: Int): CropFilter = {
    val widthAspect = destinationWidth.toDouble / originalWidth.toDouble
    val heightAspect = destinationHeight.toDouble / originalHeight.toDouble
    val scaledWidth = if (widthAspect > heightAspect) {
      destinationWidth
    } else {
      math.round(originalWidth.toDouble * heightAspect).toInt
    }
    val scaledHeight = if (widthAspect > heightAspect) {
      math.round(originalHeight.toDouble * widthAspect).toInt
    } else {
      destinationHeight
    }
    val xOffset = math.round((scaledWidth - destinationWidth) / 2.0).toInt
    val yOffset = math.round((scaledHeight - destinationHeight) / 2.0).toInt
    CropFilter(destinationWidth, destinationHeight, xOffset, yOffset)
  }

  def create(info: MediaInfo, destinationWidth: Int, destinationHeight: Int): CropFilter = {
    create(info.video.width, info.video.height, destinationWidth, destinationHeight)
  }
}