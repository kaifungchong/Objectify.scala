/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.responders

import org.objectify.adapters._
import org.objectify.ContentType._
import org.objectify.adapters.XmlResponse
import org.objectify.AcceptType
import org.objectify.adapters.JsonResponse
import org.objectify.adapters.FormattedResponse
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.objectify.executor.ObjectifyResponse

/**
 * Sample Responder class
 */
class MultipleFormatGetResponder(accept: AcceptType) extends ServiceResponder[FormattedResponse, String] {
    override def apply(serviceResult: String) = accept.content.getOrElse(JSON) match {
        case JSON => JsonResponse(serviceResult)
        case XML => XmlResponse(serviceResult)
        case CSV => CsvResponse(CsvString(serviceResult))
    }
}

case class CsvResponse(value: CsvString) extends EntityResponse[CsvString]

case class CsvString(s: String)

class CsvStringResponseAdapter extends ObjectifyResponseAdapter[CsvString] {
    override def serializeResponse(request: HttpServletRequest, response: HttpServletResponse, objectifyResponse: ObjectifyResponse[CsvString]) = {
        response.setContentType(CSV.toString)
        response.getWriter.print(objectifyResponse.entity.s)
        response.setStatus(objectifyResponse.status)
    }
}
