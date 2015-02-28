package org.objectify.responders

import org.objectify.ContentType._
import org.objectify.responders.serializers.SerializerRegistry
import org.objectify.{AcceptType, ContentType, HttpStatus}

/**
 * GenericResponder is a class that will take any supplied argument and
 * attempt to format it as JSON, XML
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
class GenericResponder(acceptType: AcceptType) extends ServiceResponder[ObjectifyResponse, Any] {
  override def apply(serviceResult: Any): ObjectifyResponse = {
    acceptType.content.getOrElse(JSON) match {
      case XML => ObjectifyResponse(SerializerRegistry.toXML(serviceResult), ContentType.XML, HttpStatus.NotImplemented)
      case JSON => ObjectifyResponse(SerializerRegistry.toJson(serviceResult), ContentType.JSON, HttpStatus.Ok)
      case _ => ObjectifyResponse("Not Implemented", ContentType.XML, HttpStatus.NotImplemented)
    }
  }
}





