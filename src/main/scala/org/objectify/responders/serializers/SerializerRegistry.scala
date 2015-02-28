package org.objectify.responders.serializers

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
object SerializerRegistry {

  // defaults
  var jsonSerializer: JsonSerializer = new JerksonJsonSerializers
  var xmlSerializer: XMLSerializer = new NotImplementedXmlSerializer

  def toJson(any: Any): String = {
    jsonSerializer(any)
  }

  def toXML(any: Any): String = {
    xmlSerializer(any)
  }

}
