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
class CurrentUserResolver extends Resolver[String, ObjectifyRequestAdapter] {
  override def apply(req: ObjectifyRequestAdapter) = "jack"
}

class ListCurrentUserResolver extends Resolver[List[String], ObjectifyRequestAdapter] {
  override def apply(req: ObjectifyRequestAdapter) = List("jack")
}

class OptionListCurrentUserResolver extends Resolver[Option[List[String]], ObjectifyRequestAdapter] {
  override def apply(req: ObjectifyRequestAdapter): Option[List[String]] = Some(List("jack"))
}
