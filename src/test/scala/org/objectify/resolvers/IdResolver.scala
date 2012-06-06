package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException

/**
  * Resolve the index from the path
  */
class IdResolver extends Resolver[Int, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getPathParameters.get("id")
            .getOrElse(throw new BadRequestException("Could not parse index from path."))
            .toInt
    }
}
