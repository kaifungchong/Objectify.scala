package org.objectify.responders

import org.objectify.ContentType._
import org.objectify.responders.serializers.SerializerRegistry
import org.objectify.services.RedirectResult
import org.objectify.{AcceptType, ContentType, HttpStatus}

/**
 * GenericResponder is a class that will take any supplied argument and
 * attempt to format it as JSON, XML
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
class GenericResponder(acceptType: AcceptType) extends ServiceResponder[ResponderResult, Any] {
  override def apply(serviceResult: Any): ResponderResult = {

    serviceResult match {
      case redirect: RedirectResult =>
        ResponderResult(SerializerRegistry.toJson(redirect), ContentType.JSON, HttpStatus.SeeOther, Map("Location" -> redirect.to))
      case _ =>
        acceptType.content.getOrElse(JSON) match {
          case XML => ResponderResult(SerializerRegistry.toXML(serviceResult), ContentType.XML, HttpStatus.NotImplemented)
          case JSON => ResponderResult(SerializerRegistry.toJson(serviceResult), ContentType.JSON, HttpStatus.Ok)
          case _ => ResponderResult("Not Implemented", ContentType.XML, HttpStatus.NotImplemented)
        }
    }
  }
}





