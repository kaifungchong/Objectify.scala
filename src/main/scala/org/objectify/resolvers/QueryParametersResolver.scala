package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Resolver for query parameters
  */
class QueryParametersResolver extends Resolver[Map[String, List[String]], ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getQueryParameters
    }
}


