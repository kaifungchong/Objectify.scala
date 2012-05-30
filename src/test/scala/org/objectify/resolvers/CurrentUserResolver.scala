package org.objectify.resolvers

import javax.servlet.http.HttpServletRequest

/**
 * Sample resolver
 *
 * @author Arthur Gonigberg
 * @since 12-05-27
 */
class CurrentUserResolver extends Resolver[String, HttpServletRequest] {
  override def apply(req: HttpServletRequest) = "jack"
}
