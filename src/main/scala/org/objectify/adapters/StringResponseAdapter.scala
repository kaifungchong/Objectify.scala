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
