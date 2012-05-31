package org.objectify.resolvers

import org.objectify.executor.ObjectifyRequest

/**
  * Sample resolver
  */
class StringResolver extends Resolver[String, ObjectifyRequest] {
    override def apply(param: ObjectifyRequest) = "johnny"
}
