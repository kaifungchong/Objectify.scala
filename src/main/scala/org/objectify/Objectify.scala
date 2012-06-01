package org.objectify

import adapters.ObjectifyRequestAdapter
import executor.{ObjectifyResponse, ObjectifyPipeline}

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions() ) {
    override def toString = {
        "Objectify Configuration" + actions.toString
    }

    def bootstrap() {
        actions.bootstrapValidation()
    }

    def execute(action: Action, requestAdapter: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
        val pipeline = new ObjectifyPipeline(this)
        pipeline.handleRequest(action, requestAdapter)
    }
}
