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
 * This exception is thrown if Objectify is not configured correctly.
 *
 * This exception is mapped to a 500 status code.
 */
class ConfigurationException(msg: String) extends ObjectifyException(500, msg) {
}
