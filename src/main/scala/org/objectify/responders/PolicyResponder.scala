package org.objectify.responders

/**
  * A policy responder is applied without any result
  */
trait PolicyResponder[T] {
    var status:Option[Int] = None
    var contentType:Option[String] = None

    def apply(): T
}
