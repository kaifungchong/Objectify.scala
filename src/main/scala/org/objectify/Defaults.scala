package org.objectify

import org.objectify.policies.Policy
import responders.PolicyResponder

case class Defaults() {

    var policies = Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]()

    def policy(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) {
        policies += policy
    }
}