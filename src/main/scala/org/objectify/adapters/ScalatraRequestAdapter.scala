/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.objectify.HttpMethod
import org.objectify.exceptions.BadRequestException
import org.scalatra.servlet.{RichResponse, RichRequest}
import org.apache.commons.fileupload.FileItem

/**
 * Scalatrafied Request!
 */
class ScalatraRequestAdapter(request: RichRequest,
                             response: RichResponse,
                             pathParameters: Map[String, String],
                             fileParams: Option[collection.Map[String, FileItem]] = None)
  extends ObjectifyRequestAdapter {

  def getPath = request.uri.toString

  def getQueryParameters = request.multiParameters.map(entry => (entry._1, entry._2.toList))

  def getPathParameters = pathParameters

  def getHttpMethod = HttpMethod.values.find(_.toString.equalsIgnoreCase(request.requestMethod.toString))
    .getOrElse(throw new BadRequestException("Could not parse HTTP method."))

  def getBody = request.body

  def getFileParams = {
    val fp = fileParams.getOrElse(Map())
    fp.toMap[String, FileItem]
  }

  def getCookies = request.cookies.toMap

  def getRequest = request.r

  def getResponse = response.res

  def getHeader(string: String): Option[String] = {
    request.header(string)
  }
}
