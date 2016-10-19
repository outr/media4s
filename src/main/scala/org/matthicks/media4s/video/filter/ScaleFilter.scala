package org.matthicks.media4s.video.filter

import org.matthicks.media4s.video.info.MediaInfo

case class ScaleFilter(width: Int, height: Int) extends VideoFilter {
  val value = s"scale=$width:$height"
}

object ScaleFilter {
  def create(originalWidth: Int, originalHeight: Int, destinationWidth: Int, destinationHeight: Int): ScaleFilter = {
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
    ScaleFilter(scaledWidth, scaledHeight)
  }

  def create(info: MediaInfo, destinationWidth: Int, destinationHeight: Int): ScaleFilter = {
    create(info.video.width, info.video.height, destinationWidth, destinationHeight)
  }
}