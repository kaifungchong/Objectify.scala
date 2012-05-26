package org.objectify.policies

import org.objectify.responders.Responder

trait Policy {
  def isAllowed:Boolean

  def getResponder:Responder
}