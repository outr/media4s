package org.matthicks.media4s.image

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ImageInfo(width: Int, height: Int, depth: Int, format: String, imageType: ImageType) {
  lazy val aspectRatio = height.toDouble / width.toDouble
  lazy val pixels = width * height
}
