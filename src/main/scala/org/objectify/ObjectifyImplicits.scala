package org.objectify

import policies.Policy
import responders.PolicyResponder

/**
  * Implicit definitions for Objectify
  */
trait ObjectifyImplicits {
    implicit def tuple2PolicyTuple(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) = new PolicyTuple(policy)
}
