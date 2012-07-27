package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest
import org.objectify.adapters.ObjectifyRequestAdapter

class HttpServletRequestResolver extends Resolver[HttpServletRequest, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter): HttpServletRequest = {
        req.getRequest
    }
}
