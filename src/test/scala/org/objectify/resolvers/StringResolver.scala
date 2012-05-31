package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest

/**
  * Sample resolver
  */
class StringResolver extends Resolver[String, HttpServletRequest] {
    override def apply(param: HttpServletRequest) = "johnny"
}
