package org.matthicks.media4s.file

import java.io.File

object FileHelpers {
  def getFileExt(fileName: String): String =
    fileName.split('.').takeRight(1).mkString.toLowerCase

  /**
    * @param suffix to be used when creating temporary file
    */
  def createTempFile(suffix: String): File =
    File.createTempFile("imageutil", "." + suffix)

  def deleteFile(file: File): Unit =
    if (!file.delete()) file.deleteOnExit()

  def replaceSuffix(n: String, s: String): String =
    s"${n.takeWhile(_ != '.')}.$s"

  /**
    * Split a filename into its bare name without extension, and the extension itself without dot.
    *
    * The extension is converted to lower-case. It's not theoretically beautiful to do that here, but it prevents a lot
    * of confusion and mistakes if we do it in one place. Which is here.
    *
    * @param fileName Input file name
    * @return         A tuple containing the extension-less filename and extension (null if not present)
    */
  def stripExt(fileName: String): (String, String) = {
    val idx = fileName.lastIndexOf('.')
    if (idx == -1) fileName -> ""
    else fileName.substring(0, idx) -> fileName.substring(idx + 1).toLowerCase
  }

  def deleteRecursively(path: File): Unit = {
    if (path.isDirectory) path.listFiles().foreach(deleteRecursively)
    if (!path.delete()) {
      throw new RuntimeException(s"Failed to delete ${path.getAbsolutePath}.")
    }
  }
}
