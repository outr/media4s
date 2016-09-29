package org.matthicks.media4s.video.info

import java.text.SimpleDateFormat

case class Tags(map: Map[String, String]) {
  def creationTime: Option[Long] = map.get("creation_time").map(time)
  def language: Option[String] = map.get("language")
  def handlerName: Option[String] = map.get("handler_name")
  def encoder: Option[String] = map.get("encoder")

  private def time(value: String): Long = {
    val dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    dateParser.parse(value).getTime
  }

  override def toString: String = s"Tags(${map.map(t => s"${t._1}=${t._2}").mkString(", ")})"
}
