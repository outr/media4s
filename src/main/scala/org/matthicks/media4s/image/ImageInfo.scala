package org.matthicks.media4s.image

import org.matthicks.media4s.Size

case class ImageInfo(width: Int, height: Int, depth: Int, format: String, imageType: Option[ImageType]) extends Size
