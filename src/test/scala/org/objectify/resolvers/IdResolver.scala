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
import org.objectify.exceptions.BadRequestException

/**
  * Resolve the index from the path
  */
class IdResolver extends Resolver[Int, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getPathParameters.get("id")
            .getOrElse(throw new BadRequestException("Could not parse index from path."))
            .toInt
    }
}
