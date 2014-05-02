/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.responders

import org.objectify.policies.Policy

/**
  * A policy responder is applied without any result
  */
trait PolicyResponder[T] {
    var status:Option[Int] = None
    var contentType:Option[String] = None
    var policy: Option[Class[_ <: Policy]] = None

    def apply(): T
}
