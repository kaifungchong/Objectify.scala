/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.{AcceptType, ContentType}

/**
 * Resolver for accept type
 */
class AcceptTypeResolver extends Resolver[AcceptType, ObjectifyRequestAdapter] {
  def apply(req: ObjectifyRequestAdapter) = {
    AcceptType(req.getHeader("Accept").map(acceptHeader => try {
      ContentType.withName(acceptHeader)
    } catch {
      case e: NoSuchElementException => ContentType.JSON
    }))
  }
}


