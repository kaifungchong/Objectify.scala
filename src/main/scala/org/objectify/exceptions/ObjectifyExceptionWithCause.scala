package org.objectify.exceptions

/**
  * Objectify exception superclass for exceptions with a cause
  */
class ObjectifyExceptionWithCause(val status: Int, msg: String, cause: Throwable) extends Exception(msg, cause) {
}
