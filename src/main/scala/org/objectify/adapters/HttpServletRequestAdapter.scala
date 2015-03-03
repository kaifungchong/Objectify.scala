/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.objectify.HttpMethod
import collection.JavaConversions
import org.objectify.exceptions.BadRequestException
import scala.io.Source

/**
 * Request adapter for HttpServletRequest
 */
class HttpServletRequestAdapter(request: HttpServletRequest, response: HttpServletResponse, pathParameters: Map[String, String])
  extends ObjectifyRequestAdapter {

  def getPath = request.getServletPath + request.getContextPath

  def getQueryParameters = convertToScala(request.getParameterMap)

  def getPathParameters = pathParameters

  def getHttpMethod = HttpMethod.values.find(_.toString.equalsIgnoreCase(request.getMethod))
    .getOrElse(throw new BadRequestException("Could not determine HTTP method."))

  def convertToScala(map: java.util.Map[String, Array[String]]): Map[String, List[String]] = {
    JavaConversions.mapAsScalaMap(map).map(entry => (entry._1, entry._2.toList)).toMap
  }

  def getBody = {
    val encoding = request.getCharacterEncoding
    val enc = if (encoding == null || encoding.trim.length == 0) {
      "ISO-8859-1"
    } else encoding
    Source.fromInputStream(request.getInputStream, enc).mkString
  }

  def getFileParams = {
    //todo
    null
  }

  def getCookies = {
    // todo
    null
  }

  def getRequest = request

  def getResponse = response

  override def getHeader(string: String): Option[String] = try {
    Some(request.getHeader(string))
  }
  catch {
    case e: NoSuchElementException => None
  }
}
