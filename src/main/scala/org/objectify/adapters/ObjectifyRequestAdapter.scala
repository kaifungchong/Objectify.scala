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

import org.apache.commons.fileupload.FileItem
import org.objectify.HttpMethod

/**
 * Adapter for requests
 */
trait ObjectifyRequestAdapter {

  //  private var _cached: Map[String, Any] = Map.empty
  //
  //  def cached: Map[String, Any] = _cached
  //
  //  def cached[T](key: String, valueOpt: Option[T]): Option[T] =
  //    valueOpt match {
  //      case Some(value) => _cached = _cached + (key -> value)
  //      case None => _cached(key)
  //    }


  def getPath: String

  def getUri: String

  def getQueryParameters: Map[String, List[String]]

  def getPathParameters: Map[String, String]

  def getHttpMethod: HttpMethod.Value

  def getBody: String

  def getFileParams: Map[String, FileItem]

  def getCookies: Map[String, String]

  def getResponse: HttpServletResponse

  def getRequest: HttpServletRequest

  def getHeader(string: String): Option[String]
}
