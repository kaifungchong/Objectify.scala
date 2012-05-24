package org.objectify

import org.objectify.policies.Policy

case class Defaults() {

    var policies: List[Class[Policy]] = Nil

    def policy(policy: String) = {

    }

    def policy(policy: Class[Policy]) = {
        policies = policy :: policies
    }
}