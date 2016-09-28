package org.matthicks.media4s.file

import java.io.File

import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
object FileDetector {
  private val EpisodeRegex1 = """(.+)[s|S](\d{1,2})[e|E](\d{2})(.*)[.]([a-zA-Z0-9]{3})""".r
  private val EpisodeRegex2 = """(.+)(\d{1,2})x(\d{2})(.*)[.]([a-zA-Z0-9]{3})""".r

  private val ExtensionRegex = """.*[.]([a-zA-Z0-9]{3})""".r

  private val DetectedExtensions = Set("mkv", "avi", "mp4", "mpeg", "mpg", "webm")

  private def detectFile(file: File): Option[DetectedFile] = file.getName match {
    case EpisodeRegex1(name, season, episode, extra, extension) if DetectedExtensions.contains(extension.toLowerCase) => {
      Some(TVEpisodeFile(
        file,
        cleanName(name),
        season.toInt,
        episode.toInt,
        extension.toLowerCase
      ))
    }
    case EpisodeRegex2(name, season, episode, extra, extension) if DetectedExtensions.contains(extension.toLowerCase) => {
      Some(TVEpisodeFile(
        file,
        cleanName(name),
        season.toInt,
        episode.toInt,
        extension.toLowerCase
      ))
    }
    case ExtensionRegex(extension) if DetectedExtensions.contains(extension.toLowerCase) => {
      throw new RuntimeException(s"Unable to detect file: $file.")
    }
    case _ => {
      println(s"Skipping: $file")
      None
    }
  }

  private def cleanName(name: String) = {
    name.replaceAll("[.]", "").replaceAll("-", "").trim
  }

  private def detectDirectory(file: File, detected: ListBuffer[DetectedFile]): Unit = file.listFiles().foreach {
    case f if f.isDirectory => detectDirectory(f, detected)
    case f => detectFile(f) match {
      case Some(df) => {
        println(df)
        detected += df
      }
      case None => // Ignore
    }
  }
}

trait DetectedFile {
  def file: File
}

case class TVEpisodeFile(file: File, name: String, season: Int, episode: Int, extension: String) extends DetectedFile {
  override def toString = s"Name: $name, Season: $season, Episode: $episode, Extension: $extension"
}