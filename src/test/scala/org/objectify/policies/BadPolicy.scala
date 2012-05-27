package org.objectify.policies

import org.objectify.responders.BadPolicyResponder

/**
 * Sample policy
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class BadPolicy extends Policy {
  def isAllowed = false

  def getResponder = classOf[BadPolicyResponder]
}
