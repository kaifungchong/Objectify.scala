/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.objectify.executor.ObjectifyResponse
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
  * Response adapter for String
  */
class StringResponseAdapter extends ObjectifyResponseAdapter[String] {
    def serializeResponse(request: HttpServletRequest, response: HttpServletResponse,
                          objectifyResponse: ObjectifyResponse[String]) {

        response.setContentType(objectifyResponse.contentType)
        response.setStatus(objectifyResponse.status)
        response.getWriter.print(objectifyResponse.entity)
    }
}
