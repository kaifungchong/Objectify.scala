package org.objectify.adapters

import javax.servlet.http.HttpServletRequest
import org.objectify.HttpMethod
import collection.JavaConversions
import org.objectify.exceptions.BadRequestException

/**
  * Request adapter for HttpServletRequest
  */
class HttpServletRequestAdapter(request: HttpServletRequest) extends ObjectifyRequestAdapter {

    def getPath = request.getServletPath + request.getContextPath

    def getQueryParameters = convertToScala(request.getParameterMap)

    def getHttpMethod = HttpMethod.values.find(_.toString.equalsIgnoreCase(request.getMethod))
        .getOrElse(throw new BadRequestException("Could not determine HTTP method."))

    def convertToScala(map: java.util.Map[String, Array[String]]): Map[String, Array[String]] = {
        JavaConversions.mapAsScalaMap(map).toMap
    }
}
