package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Resolver for query parameters
  */
class PathParametersResolver extends Resolver[Map[String, String], ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getPathParameters
    }
}
