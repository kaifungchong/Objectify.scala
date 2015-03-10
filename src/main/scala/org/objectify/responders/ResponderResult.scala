package org.objectify.responders

import org.objectify.ContentType.ContentType
import org.objectify.HttpStatus.HttpStatus

/**
 * Generic Container for responses formatted as strings
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
case class ResponderResult(value: String,
                           contentType: ContentType,
                           httpStatus: HttpStatus)
