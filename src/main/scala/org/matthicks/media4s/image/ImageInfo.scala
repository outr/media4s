package org.matthicks.media4s.image

import org.matthicks.media4s.Info

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ImageInfo(width: Int, height: Int, depth: Int, format: String, imageType: ImageType) extends Info
