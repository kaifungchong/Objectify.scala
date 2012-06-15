package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Resolve the body as JSON
  */
class BodyResolver extends Resolver[String, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getBody
    }
}
