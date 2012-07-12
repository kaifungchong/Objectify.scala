package org.objectify

import policies.Policy
import responders.PolicyResponder

/**
  * Implicit definitions for Objectify
  */
trait ObjectifyImplicits {
    implicit def tuple2PolicyTuple(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) = new PolicyTuple(policy)

    implicit def map2PolicyTupleSeq(policyMap: Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]) = {
        policyMap.map {
            case (pol, responder) => new PolicyTuple(pol, responder)
        }(collection.breakOut): Seq[PolicyTuple]
    }
}
