package org.objectify.policies


trait Policy {
  def isAllowed: Boolean
}