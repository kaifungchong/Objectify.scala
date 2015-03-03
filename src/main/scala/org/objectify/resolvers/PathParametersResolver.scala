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

/**
 * Resolver for query parameters
 */
class PathParametersResolver extends Resolver[Map[String, String], ObjectifyRequestAdapter] {
  def apply(req: ObjectifyRequestAdapter) = {
    req.getPathParameters
  }
}
