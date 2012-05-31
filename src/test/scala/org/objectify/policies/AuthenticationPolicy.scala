package org.objectify.policies

import javax.inject.Named

/**
  * Sample policy
  */
class AuthenticationPolicy(@Named("CurrentUserResolver") user: String, string: String) extends Policy {
    def isAllowed = {
        user != null && string != null
    }
}