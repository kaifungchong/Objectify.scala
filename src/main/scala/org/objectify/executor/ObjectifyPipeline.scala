package org.objectify.executor

import org.objectify.policies.Policy
import org.objectify.{Action, Objectify}
import org.objectify.services.Service
import org.objectify.responders.{PolicyResponder, ServiceResponder}
import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * This class is responsible for executing the pipeline for the lifecycle of a request.
  */
class ObjectifyPipeline(objectify: Objectify) {

    def handleRequest(action: Action, req: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
        // execute policies
        val policyResponders =
            for {(policy, responder) <- action.resolvePolicies
                 if (!instantiate[Policy](policy, req).isAllowed)} yield responder

        // if policies failed respond with first failure
        if (policyResponders.nonEmpty) {
            generateResponse(instantiate[PolicyResponder[_]](policyResponders.head, req).apply())
        }
        // else execute the service call
        else {
            val service = instantiate[Service[_]](action.resolveServiceClass, req)
            val responder = instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)

            generateResponse(responder.applyAny(service()))
        }
    }

    private def instantiate[T: ClassManifest](klass: Class[_ <: T], req: ObjectifyRequestAdapter) = {
        Invoker.invoke(klass, req)
    }

    def generateResponse(result: Any): ObjectifyResponse[_] = {
        new ObjectifyResponse("text/plain", 200, result)
    }
}

