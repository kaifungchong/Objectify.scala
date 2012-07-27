package org.objectify.resolvers

import javax.servlet.ServletContext
import org.objectify.adapters.ObjectifyRequestAdapter
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Resolver for the servlet context
  */
class ServletContextResolver extends Resolver[ServletContext, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter): ServletContext = {
        req.getRequest.getServletContext
    }
}




