/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import org.objectify.ContentType.ContentType
import org.objectify.HttpStatus.HttpStatus

import scala.reflect.ClassTag

/**
 * This is a wrapper for a response
 */
class ObjectifyResponse[T: ClassTag](val contentType: ContentType, val status: HttpStatus, val entity: T)


