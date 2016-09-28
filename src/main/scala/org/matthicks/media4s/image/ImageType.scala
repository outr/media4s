package org.matthicks.media4s.image

/**
 * @author Matt Hicks <matt@outr.com>
 */
sealed abstract class ImageType(val extensions: List[String], val mimeType: String) {
  lazy val extension = extensions.head
}

object ImageType {
  case object JPEG extends ImageType(List("jpg", "jpeg"), "image/jpeg")
  case object PNG extends ImageType(List("png"), "image/png")
  case object GIF extends ImageType(List("gif"), "image/gif")
  case object SVG extends ImageType(List("svg"), "image/svg+xml")
  case object TIFF extends ImageType(List("tif", "tiff"), "image/tiff")
  case object BMP extends ImageType(List("bmp"), "image/bmp")
  case object TGA extends ImageType(List("tga", "targa"), "image/x-tga")

  val values = Vector(JPEG, PNG, GIF, SVG, TIFF, BMP, TGA)

  def fromExtension(ext: String) = values.find(it => it.extensions.contains(ext.toLowerCase))
}
