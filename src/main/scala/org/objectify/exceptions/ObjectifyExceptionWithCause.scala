/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.exceptions

/**
  * Objectify exception superclass for exceptions with a cause
  */
class ObjectifyExceptionWithCause(val status: Int, msg: String, cause: Throwable) extends Exception(msg, cause) {
}
