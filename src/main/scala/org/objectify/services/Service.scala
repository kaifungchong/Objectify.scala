package org.objectify.services

/**
  * Services are intended to do most of the heavy lifting and business logic.
  */
trait Service[T] {
    def apply():T
}