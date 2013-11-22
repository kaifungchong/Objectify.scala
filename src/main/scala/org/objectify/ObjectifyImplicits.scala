/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify

import policies.Policy
import responders.PolicyResponder

/**
  * Implicit definitions for Objectify sexiness
  */
trait ObjectifyImplicits {
    implicit def string2optionString(s: String) = Some(s)

    implicit def tuple2PolicyTuple(policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) = new PolicyTuple(policy)

    implicit def map2PolicyTupleSeq(policyMap: Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]) = {
        policyMap.map {
            case (pol, responder) => new PolicyTuple(pol, responder)
        }(collection.breakOut): Seq[PolicyTuple]
    }
}
