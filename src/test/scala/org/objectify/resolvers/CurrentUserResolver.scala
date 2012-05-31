package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest

/**
  * Sample resolver
  */
class CurrentUserResolver extends Resolver[String, HttpServletRequest] {
    override def apply(req: HttpServletRequest) = "jack"
}
