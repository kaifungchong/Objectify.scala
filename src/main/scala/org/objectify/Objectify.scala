package org.objectify

import adapters.ObjectifyRequestAdapter
import executor.{ObjectifyResponse, ObjectifyPipeline}
import com.twitter.logging.Logger
import responders.ServiceResponder

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions())
    extends ObjectifySugar with ObjectifyImplicits {
    private val logger = Logger(classOf[Objectify])

    override def toString = {
        "Objectify Configuration" + actions.toString
    }

    def bootstrap() {
        actions.bootstrapValidation()
    }

    def execute(action: Action, requestAdapter: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
        val pipeline = new ObjectifyPipeline(this)
        val start = System.currentTimeMillis()
        val response = pipeline.handleRequest(action, requestAdapter)
        logger.info("Handled request for [%s] successfully in [%sms].".format(action, (System.currentTimeMillis() - start)))

        response
    }

    var postServiceHook = (serviceResult:Any, responder: ServiceResponder[_,_]) => {}
}
