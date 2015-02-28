/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.objectify.executor.ObjectifyResponse
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.objectify.ContentType._
import org.objectify.resolvers.ClassResolver
import org.objectify.responders.ResponderResult

/**
 * Response adapter for String
 */
class FormattedResponseAdapter extends ObjectifyResponseAdapter[FormattedResponse] {
  def serializeResponse(request: HttpServletRequest, response: HttpServletResponse, objectifyResponse: ObjectifyResponse[FormattedResponse]) {

    objectifyResponse.entity.response match {
      case r: JsonResponse =>
        response.setContentType(JSON.toString)
        response.getWriter.print(r.value)
        response.setStatus(objectifyResponse.status.id)
      case r: XmlResponse =>
        response.setContentType(XML.toString)
        response.getWriter.print(r.value)
        response.setStatus(objectifyResponse.status.id)
      case r: HtmlResponse =>
        response.setContentType(HTML.toString)
        response.getWriter.print(r.value)
        response.setStatus(objectifyResponse.status.id)
      case r: TextResponse =>
        response.setContentType(TEXT.toString)
        response.getWriter.print(r.value)
        response.setStatus(objectifyResponse.status.id)
      case r: ResponderResult =>
        response.setContentType(r.contentType.toString)
        response.getWriter.print(r.value)
        response.setStatus(objectifyResponse.status.id)
      case _ =>
        // load adapter by class
        val newEntity = objectifyResponse.entity.response.value
        val newResponse = new ObjectifyResponse(objectifyResponse.contentType, objectifyResponse.status, newEntity)
        ClassResolver.locateResponseAdapter(newResponse).serializeResponse(request, response, newResponse)
    }
  }
}

case class FormattedResponse(response: EntityResponse[_])

/** Entity Response types **/

trait EntityResponse[T] {
  def value: T
}

case class JsonResponse(value: String) extends EntityResponse[String]

case class XmlResponse(value: String) extends EntityResponse[String]

case class HtmlResponse(value: String) extends EntityResponse[String]

case class TextResponse(value: String) extends EntityResponse[String]
