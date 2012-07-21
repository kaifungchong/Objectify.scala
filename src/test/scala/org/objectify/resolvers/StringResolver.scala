package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * Sample resolver
  */
class StringResolver extends Resolver[String, ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter) = "johnny"
}

class ListStringResolver extends Resolver[List[String], ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter) = List("johnny")
}

class OptionListStringResolver extends Resolver[Option[List[String]], ObjectifyRequestAdapter] {
    override def apply(param: ObjectifyRequestAdapter): Option[List[String]] = Some(List("johnny"))
}
