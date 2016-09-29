package org.matthicks.media4s.video

case class MetaData(map: Map[String, Any] = Map.empty) {
  def apply(key: String, value: String) = copy(map + (key -> MetaData.fromString(value)))

  def get[T](key: String) = map.get(key).asInstanceOf[Option[T]]
  def apply[T](key: String) = map(key).asInstanceOf[T]

  override def toString = map.map(t => s"${t._1} = ${t._2}").mkString(", ")
}

object MetaData {
  private val Integer = """\d+""".r
  private val Double = """[0-9.-]+""".r
  private val Date = """(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})""".r

  def fromString(s: String) = s match {
    case Integer(value) => value.toInt
    case Double(value) => value.toDouble
    case Date(year, month, day, hour, minute, second) => {
      val calendar = Calendar.getInstance()
      calendar.set(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt)
      calendar.getTimeInMillis
    }
    case _ => s
  }
}