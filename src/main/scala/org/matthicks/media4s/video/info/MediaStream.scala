package org.matthicks.media4s.video.info

import fabric.rw._

case class MediaStream(index: Int,
                       codec_name: Option[String],
                       codec_long_name: Option[String],
                       profile: String = "",
                       codec_type: String,
                       codec_time_base: Option[String],
                       codec_tag_string: String,
                       codec_tag: String,
                       width: Int = 0,
                       height: Int = 0,
                       coded_width: Int = 0,
                       coded_height: Int = 0,
                       has_b_frames: Int = 0,
                       sample_fmt: String = "",
                       sample_rate: Long = 0L,
                       channels: Int = 0,
                       channel_layout: String = "",
                       sample_aspect_ratio: String = "",
                       display_aspect_ratio: String = "",
                       pix_fmt: String = "",
                       level: Int = 0,
                       chroma_location: String = "",
                       field_order: String = "",
                       refs: Int = 0,
                       is_avc: Boolean = false,
                       nal_length_size: Int = 0,
                       r_frame_rate: String,
                       avg_frame_rate: String,
                       time_base: String,
                       duration_ts: Long = 0L,
                       duration: BigDecimal = BigDecimal(0),
                       start_time: BigDecimal = BigDecimal(0),
                       bit_rate: Long = 0L,
                       bits_per_sample: Int = 0,
                       bits_per_raw_sample: Int = 0)

object MediaStream {
  implicit val rw: RW[MediaStream] = RW.gen
}