package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Sample resolver
  */
class CurrentUserResolver extends Resolver[String, ObjectifyRequestAdapter] {
    override def apply(req: ObjectifyRequestAdapter) = "jack"
}
