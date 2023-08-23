package org.matthicks.media4s.video.info

import fabric.rw._

case class MediaFormat(filename: String,
                       start_time: Double = 0.0,
                       nb_streams: Int,
                       nb_programs: Int,
                       format_name: String,
                       format_long_name: String,
                       duration: BigDecimal,
                       size: Long,
                       bit_rate: Long,
                       probe_score: Int,
                       tags: Map[String, String])

object MediaFormat {
  implicit val rw: RW[MediaFormat] = RW.gen
}