package org.objectify.resolvers

import javax.servlet.http.HttpServletResponse
import org.objectify.adapters.ObjectifyRequestAdapter

class HttpServletResponseResolver extends Resolver[HttpServletResponse, ObjectifyRequestAdapter] {
     def apply(req: ObjectifyRequestAdapter): HttpServletResponse = {
         req.getResponse
     }
 }
