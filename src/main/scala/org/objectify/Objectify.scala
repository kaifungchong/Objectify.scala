package org.objectify

import adapters.{ObjectifyResponseAdapter, ObjectifyRequestAdapter}
import executor.{ObjectifyResponse, ObjectifyPipeline}
import com.twitter.logging.Logger
import resolvers.ClassResolver
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
        try {
            pipeline.handleRequest(action, requestAdapter)
        }
        finally {
            // want to get a logging statement even if request throws exception
            val requestTime = System.currentTimeMillis() - start
            logger.info("Request [%s - %s] took [%sms] for action [%s]."
                .format(requestAdapter.getHttpMethod, requestAdapter.getPath, requestTime, action))
        }
    }

    def locateResponseAdapter(response: ObjectifyResponse[_]): ObjectifyResponseAdapter[_] = {
        ClassResolver.resolveResponseAdapter(response.getClass).newInstance()
    }

    var postServiceHook = (serviceResult: Any, responder: ServiceResponder[_, _]) => {}
}
