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
import org.objectify.{ContentType, AcceptType}

/**
 * Resolver for accept type
 */
class AcceptTypeResolver extends Resolver[AcceptType, ObjectifyRequestAdapter] {
  def apply(req: ObjectifyRequestAdapter) = {
    val acceptType = try {
      val acceptString = req.getRequest.getHeader("Accept")
      if (acceptString != null) Some(ContentType.withName(acceptString)) else None
    }
    catch {
      case e: NoSuchElementException => {
        e.printStackTrace()
        None
      }
    }

    AcceptType(acceptType)
  }
}


