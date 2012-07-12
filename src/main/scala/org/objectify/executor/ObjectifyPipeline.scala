package org.objectify.executor

import org.objectify.policies.Policy
import org.objectify.services.Service
import org.objectify.responders.{PolicyResponder, ServiceResponder}
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.ObjectifyException
import org.objectify.{ContentType, Action, Objectify}
import com.twitter.logging.Logger

/**
  * This class is responsible for executing the pipeline for the lifecycle of a request.
  */
class ObjectifyPipeline(objectify: Objectify) {
    private val logger = Logger(classOf[ObjectifyPipeline])

    def handleRequest(action: Action, req: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
        // determine which policies to execute -- globals + action defaults or globals + action overrides
        val policiesToExecute = getPolicies(action)

        // execute policies
        val policyResponders = {
            for {(policy, responder) <- policiesToExecute
                 if (!instantiate[Policy](policy, req).isAllowed)} yield responder
        }

        // if policies failed respond with first failure
        if (policyResponders.nonEmpty) {
            generateResponse(() => {
                instantiate[PolicyResponder[_]](policyResponders.head, req).apply()
            }, ContentType.getTypeString(action.contentType))
        }
        // else execute the service call
        else {
            val service = instantiate[Service[_]](action.resolveServiceClass, req)
            val responder = instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)

            generateResponse(() => {
                responder.applyAny(service())
            }, ContentType.getTypeString(action.contentType))
        }
    }

    private def getPolicies(action: Action) = {
        if (!action.ignoreGlobalPolicies)
            objectify.defaults.globalPolicies ++ getLocalPolicies(action)
        else
            getLocalPolicies(action)
    }

    private def getLocalPolicies(action: Action) = {
        if (action.resolvePolicies.nonEmpty)
            action.resolvePolicies
        else
            objectify.defaults.defaultPolicies
    }

    private def instantiate[T: ClassManifest](klass: Class[_ <: T], req: ObjectifyRequestAdapter) = {
        Invoker.invoke(klass, req)
    }

    def generateResponse(result: () => Any, contentType: String): ObjectifyResponse[_] = {
        try {
            val content = result()
            new ObjectifyResponse(contentType, 200, content)
        }
        catch {
            case e: ObjectifyException => {
                logger.error(e, "Could not complete request")
                new ObjectifyResponse("text/plain", e.status, e.getMessage)
            }
            case e: Exception => {
                logger.error(e, "Unexpected exception")
                new ObjectifyResponse("text/plain", 500, e.getMessage)
            }
        }
    }
}

