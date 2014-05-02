/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import javax.servlet.http.HttpServletResponse
import org.objectify.adapters.ObjectifyRequestAdapter

/**
 * Included resolver for Java HTTP Response
 */
class HttpServletResponseResolver extends Resolver[HttpServletResponse, ObjectifyRequestAdapter] {
     def apply(req: ObjectifyRequestAdapter): HttpServletResponse = {
         req.getResponse
     }
 }
