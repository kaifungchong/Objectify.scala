package org.objectify.exceptions

/**
  * This exception geared specifically for bad requests data.
  *
  * This exception is mapped to a 400 status code.
  */
class BadRequestException(msg: String) extends ObjectifyException(400, msg) {
}
