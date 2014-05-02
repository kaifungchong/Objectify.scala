/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.policies

/**
  * The policy interface allows you to implement policies, which get executed before the services. The policies
  * are executed in a series, and the first one to fail will be the one to respond.
  */
trait Policy {
    def isAllowed: Boolean
}