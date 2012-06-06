package org.objectify.adapters

import javax.servlet.http.HttpServletRequest
import org.objectify.HttpMethod
import collection.JavaConversions
import org.objectify.exceptions.BadRequestException

/**
  * Request adapter for HttpServletRequest
  */
class HttpServletRequestAdapter(request: HttpServletRequest, pathParameters: Map[String, String]) extends ObjectifyRequestAdapter {

    def getPath = request.getServletPath + request.getContextPath

    def getQueryParameters = convertToScala(request.getParameterMap)

    def getPathParameters = pathParameters

    def getHttpMethod = HttpMethod.values.find(_.toString.equalsIgnoreCase(request.getMethod))
        .getOrElse(throw new BadRequestException("Could not determine HTTP method."))

    def convertToScala(map: java.util.Map[String, Array[String]]): Map[String, List[String]] = {
        JavaConversions.mapAsScalaMap(map).map(entry => (entry._1, entry._2.toList) ).toMap
    }
}
