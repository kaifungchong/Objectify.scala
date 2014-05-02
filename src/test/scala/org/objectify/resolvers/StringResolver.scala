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
  * Sample resolver
  */
class StringResolver extends Resolver[String, ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter) = "johnny"
}

class ListStringResolver extends Resolver[List[String], ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter) = List("johnny")
}

class OptionListStringResolver extends Resolver[Option[List[String]], ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter): Option[List[String]] = Some(List("johnny"))
}
