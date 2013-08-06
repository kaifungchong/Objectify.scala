/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest
import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Included resolver for Java HTTP Request
  */
class HttpServletRequestResolver extends Resolver[HttpServletRequest, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter): HttpServletRequest = {
        req.getRequest
    }
}
