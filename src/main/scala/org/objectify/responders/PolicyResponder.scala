package org.objectify.responders

/**
  * A policy responder is applied without any result
  */
trait PolicyResponder[T] {
    var status:Option[Int] = None

    def apply(): T
}
