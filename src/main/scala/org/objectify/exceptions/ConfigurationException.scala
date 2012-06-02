package org.objectify.exceptions

/**
  * This exception is thrown if Objectify is not configured correctly.
  *
  * This exception is mapped to a 500 status code.
  */
class ConfigurationException(msg: String) extends ObjectifyException(500, msg) {
}
