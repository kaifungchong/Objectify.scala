package org.objectify.responders


/**
  * Sample responder
  */
class BadPolicyResponder extends PolicyResponder[String] {
    override def apply(): String = {
        "Sweet"
    }
}
