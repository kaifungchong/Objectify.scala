package org.objectify.responders

import org.objectify.services.Service

/**
 * Sample responder
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class BadPolicyResponder extends Responder {
  override def apply(service: Option[_ <: Service]):String = {
    "Sweet"
  }
}
