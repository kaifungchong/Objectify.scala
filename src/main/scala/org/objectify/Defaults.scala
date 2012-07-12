package org.objectify

import org.objectify.policies.Policy
import responders.PolicyResponder

case class Defaults() {

    var defaultPolicies = Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]()
    var globalPolicies = Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]()

    def policy(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) {
        defaultPolicies += policy
    }

    def globalPolicy(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) {
        globalPolicies += policy
    }
}