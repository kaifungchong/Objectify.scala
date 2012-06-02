package org.objectify.exceptions

/**
  * Objectify exception super class
  */
class ObjectifyException(val status: Int, msg: String) extends Exception(msg) {
}


