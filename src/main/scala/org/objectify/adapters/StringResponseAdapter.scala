/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.objectify.executor.ObjectifyResponse

/**
 * Response adapter for String
 */
class StringResponseAdapter extends ObjectifyResponseAdapter[String] {
  def serializeResponse(request: HttpServletRequest, response: HttpServletResponse,
                        objectifyResponse: ObjectifyResponse[String]) {

    response.setContentType(objectifyResponse.contentType.toString)
    response.setStatus(objectifyResponse.status.id)
    response.getWriter.print(objectifyResponse.entity)
  }
}
