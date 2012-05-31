package org.objectify.adapters

/**
  * Response adapters are an easy way to allow for serializing any type you desire
  */
trait ObjectifyResponseAdapter[T] {
    def serializeResponse(dto: T): String
}
