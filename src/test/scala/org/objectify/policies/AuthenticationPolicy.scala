package org.objectify.policies


/**
 * Sample policy
 *
 * @author Arthur Gonigberg
 * @since 12-05-27
 */
class AuthenticationPolicy extends Policy {
  var callCurrentUser: String = null

  def isAllowed = {
    callCurrentUser != null
  }
}