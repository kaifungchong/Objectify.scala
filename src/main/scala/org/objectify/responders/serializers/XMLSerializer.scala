package org.objectify.responders.serializers

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
trait XMLSerializer {
  def apply(any: Any):String
}

class NotImplementedXmlSerializer extends XMLSerializer {
  override def apply(any: Any): String = "<message>Not Implemented<message>"
}
