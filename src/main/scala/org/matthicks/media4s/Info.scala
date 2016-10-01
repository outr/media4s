package org.matthicks.media4s

trait Info {
  def width: Int
  def height: Int

  lazy val pixels = width * height
  lazy val aspectRatio: Double = height.toDouble / width.toDouble

  def heightForWidth(width: Int): Int = math.round(width.toDouble * aspectRatio).toInt

  def widthForHeight(height: Int): Int = math.round(height.toDouble / aspectRatio).toInt
}
