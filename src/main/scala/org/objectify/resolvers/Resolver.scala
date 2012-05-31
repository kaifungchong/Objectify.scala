package org.objectify.resolvers

/**
  * Interface for resolvers. Resolvers are intended to help Objectify figure out how to inject
  * the constructors for various types in the application.
  */
trait Resolver[T, P] {
    def apply(param: P): T
}
