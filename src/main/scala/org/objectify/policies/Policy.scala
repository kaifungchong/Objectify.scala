package org.objectify.policies

/**
  * The policy interface allows you to implement policies, which get executed before the services. The policies
  * are executed in a series, and the first one to fail will be the one to respond.
  */
trait Policy {
    def isAllowed: Boolean
}