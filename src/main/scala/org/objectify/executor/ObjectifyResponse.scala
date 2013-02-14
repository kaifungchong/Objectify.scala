package org.objectify.executor

/**
  * This is a wrapper for a response
  */
class ObjectifyResponse[T: ClassManifest](val contentType: String, val status: Int, val entity: T)


