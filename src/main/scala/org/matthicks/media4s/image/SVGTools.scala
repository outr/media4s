package org.matthicks.media4s.image

import java.io.File

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Attribute, Elem, Node, Null, XML}

object SVGTools {
  def colorize(input: File, colorHex: String, output: File): Unit = {
    val elem = load(input)
    val rule = new RewriteRule {
      override def transform(n: Node): collection.Seq[Node] = n match {
        case elem: Elem if elem.label == "path" => {
          var e = elem
          val fill = e \@ "fill"
          val stroke = e \@ "stroke"
          if (fill != "none") {
            e = e % Attribute(null, "fill", colorHex, Null)
          }
          if (stroke.nonEmpty) {
            e = e % Attribute(null, "stroke", colorHex, Null)
          }
          e
        }
        case _ => n
      }
    }
    val transform = new RuleTransformer(rule)
    val modified = transform.transform(elem).head
    XML.save(output.getAbsolutePath, modified)
  }

  def load(file: File): Elem = XML.loadFile(file)
}
