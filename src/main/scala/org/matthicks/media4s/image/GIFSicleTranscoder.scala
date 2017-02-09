package org.matthicks.media4s.image

import java.io.File

import org.matthicks.media4s.TranscodeFailedException

import scala.sys.process._

case class GIFSicleTranscoder(input: File, output: File, args: List[GIFSicleArgument] = Nil) {
  lazy val command: List[String] = {
    "gifsicle" :: args.flatMap(_.args.map(_.toString)) ::: List(input.getAbsolutePath)
  }

  def withArgs(arguments: Any*): GIFSicleTranscoder = {
    copy(args = args ::: List(GIFSicleArgument(arguments.toList)))
  }

  def optimize(level: Int): GIFSicleTranscoder = {
    withArgs(s"--optimize=$level")
  }

  def crop(x1: Int, y1: Int, x2: Int, y2: Int): GIFSicleTranscoder = {
    withArgs("--crop", s"$x1,$y1-$x2,$y2")
  }

  def resize(width: Option[Int] = None, height: Option[Int] = None): GIFSicleTranscoder = {
    withArgs("--resize", s"${width.getOrElse("_")}x${height.getOrElse("_")}")
  }

  def resizeFit(width: Option[Int] = None, height: Option[Int] = None): GIFSicleTranscoder = {
    withArgs("--resize-fit", s"${width.getOrElse("_")}x${height.getOrElse("_")}")
  }

  def execute(): Unit = {
    val result = (command #> output) ! ProcessLogger((line: String) => println(line))
    if (result != 0) {
      throw new TranscodeFailedException(s"Failed transcoding (${command.mkString(" ")}). Received result: $result.")
    }
  }
}

case class GIFSicleArgument(args: List[Any])