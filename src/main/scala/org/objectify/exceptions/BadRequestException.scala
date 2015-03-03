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
 * This exception geared specifically for bad requests data.
 *
 * This exception is mapped to a 400 status code.
 */
class BadRequestException(msg: String) extends ObjectifyException(400, msg) {
}
