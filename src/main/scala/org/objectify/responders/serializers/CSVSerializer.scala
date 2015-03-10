package org.objectify.responders.serializers

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
trait CSVSerializer {
  def apply(any: Any): String
}

class NotImplementedCSVSerializer extends XMLSerializer {
  override def apply(any: Any): String = "<message>Not Implemented<message>"
}
