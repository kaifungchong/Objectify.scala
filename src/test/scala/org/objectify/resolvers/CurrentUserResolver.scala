package org.objectify.resolvers

import org.objectify.executor.ObjectifyRequest

/**
  * Sample resolver
  */
class CurrentUserResolver extends Resolver[String, ObjectifyRequest] {
    override def apply(req: ObjectifyRequest) = "jack"
}
