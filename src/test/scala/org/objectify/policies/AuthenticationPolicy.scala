package org.objectify.policies

import javax.inject.Named


/**
 * Sample policy
 *
 * @author Arthur Gonigberg
 * @since 12-05-27
 */
class AuthenticationPolicy(@Named("CurrentUserResolver") val user: String) extends Policy {
  def isAllowed = {
    user != null
  }
}