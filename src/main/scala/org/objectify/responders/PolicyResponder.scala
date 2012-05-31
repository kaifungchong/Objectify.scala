package org.objectify.responders

/**
  * A policy responder is applied without any result
  */
trait PolicyResponder[T] {
    def apply(): T
}
