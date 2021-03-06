/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.policies

import javax.inject.Named

/**
 * Sample policy
 */
class AuthenticationPolicy(@Named("CurrentUser") user: String, string: String) extends Policy {
  def isAllowed = {
    user != null && string != null
  }
}