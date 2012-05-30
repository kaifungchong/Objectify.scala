package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest

/**
  * Sample resolver
  *
  * @author Arthur Gonigberg
  * @since 12-05-29
  */
class StringResolver extends Resolver[String, HttpServletRequest] {
    override def apply(param: HttpServletRequest) = "johnny"
}
