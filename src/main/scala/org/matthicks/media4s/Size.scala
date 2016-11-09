package org.matthicks.media4s

trait Size {
  def width: Int
  def height: Int

  lazy val pixels = width * height
  lazy val aspectRatio: Double = height.toDouble / width.toDouble

  def heightForWidth(width: Int): Int = math.round(width.toDouble * aspectRatio).toInt

  def widthForHeight(height: Int): Int = math.round(height.toDouble / aspectRatio).toInt

  def scaleUp(width: Option[Int], height: Option[Int]): Size = {
    var w = this.width
    var h = this.height
    if (width.getOrElse(this.width) > w) {
      w = width.get
      h = heightForWidth(w)
    }
    if (height.getOrElse(this.height) > h) {
      h = height.get
      w = widthForHeight(h)
    }
    Size(w, h)
  }

  def cropToAspectRatio(aspectRatio: Double): Size = {
    if (this.aspectRatio > aspectRatio) {
      Size(width, math.round(width.toDouble * aspectRatio).toInt)
    } else {
      Size(math.round(height.toDouble / aspectRatio).toInt, height)
    }
  }

  def cropToAspectRatio(size: Size): Size = cropToAspectRatio(size.aspectRatio)
}

object Size {
  def apply(width: Int, height: Int): Size = SimpleSize(width, height)

  case class SimpleSize(width: Int, height: Int) extends Size
}