package org.objectify.resolvers

/**
  * Interface for resolvers
  *
  * @author Arthur Gonigberg
  * @since 12-05-27
  */
trait Resolver[T, P] {
    def apply(param: P): T
}
