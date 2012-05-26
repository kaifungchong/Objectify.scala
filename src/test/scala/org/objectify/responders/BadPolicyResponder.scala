package org.objectify.responders

/**
 * Sample responder
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class BadPolicyResponder extends Responder {
  override def apply():String = {
    "Sweet"
  }
}
