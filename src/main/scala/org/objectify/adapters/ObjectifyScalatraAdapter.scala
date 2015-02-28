/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.objectify.HttpMethod.{Delete, Get, Options, Patch, Post, Put}
import org.objectify.Objectify
import org.objectify.exceptions.{BadRequestException, ObjectifyException, ObjectifyExceptionWithCause}
import org.objectify.resolvers.ClassResolver
import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.servlet.{RichRequest, RichResponse, ServletBase}

trait ObjectifyScalatraAdapter extends Objectify with ServletBase with FileUploadSupport {

  /**
   * Decorates the default bootstrap which has the configuration
   * validation in it
   */
  override def bootstrap() {
    super.bootstrap()

    /**
     * Sort wildcards to the top so that there are no route conflicts and that routes are always
     * added consistently.
     */
    val sortedActions = actions.toList.sortBy(_.route.getOrElse(""))(new Ordering[String] {
      def compare(x: String, y: String) = {
        val wildcardSymbol = ':'
        if (x.indexOf(wildcardSymbol) != -1 && y.indexOf(wildcardSymbol) != -1) {
          0
        }
        else if (x.indexOf(wildcardSymbol) != -1) {
          -1
        }
        else if (y.indexOf(wildcardSymbol) != -1) {
          1
        }
        else {
          0
        }
      }
    })

    /**
     * For each action we map to the Scalatra equivalent block parameter
     * though we are still bound to the HttpServletRequest and Response
     * currently.
     */
    sortedActions.foreach(action => {
      def scalatraFunction(route: String) = action.method match {
        case Options => options(route) _
        case Get => get(route) _
        case Post => post(route) _
        case Put => put(route) _
        case Delete => delete(route) _
        case Patch => patch(route) _
      }

      scalatraFunction("/" + action.route.getOrElse(throw new BadRequestException("No Route Found"))) {
        // wrap HttpServletRequest in adapter and get ObjectifyResponse
        val objectifyResponse = execute(action,
          new ScalatraRequestAdapter(RichRequest(request), RichResponse(response), params.toMap, Some(fileParams)))

        // find appropriate response adapter and serialize the response
        ClassResolver.locateResponseAdapter(objectifyResponse).serializeResponseAny(request, response, objectifyResponse)
      }
    })
  }

  error {
    case e: ObjectifyException => {
      status = e.status
      e.printStackTrace()
      e.getMessage
    }
    case e: ObjectifyExceptionWithCause => {
      status = e.status
      e.printStackTrace()
      e.getMessage
    }
  }
}

