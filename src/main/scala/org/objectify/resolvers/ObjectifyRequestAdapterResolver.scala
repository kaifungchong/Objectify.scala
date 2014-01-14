/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
 * Resolver for Objectify request adapter
 */
class ObjectifyRequestAdapterResolver extends Resolver[ObjectifyRequestAdapter, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = req
}
