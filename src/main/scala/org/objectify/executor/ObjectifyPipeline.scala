/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import org.objectify.policies.Policy
import org.objectify.services.Service
import org.objectify.responders.{PolicyResponder, ServiceResponder}
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.{ObjectifyExceptionWithCause, ObjectifyException}
import org.objectify.{ContentType, Action, Objectify}

/**
  * This class is responsible for executing the pipeline for the lifecycle of a request.
  */
class ObjectifyPipeline(objectify: Objectify) {
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
            val responder = instantiate[PolicyResponder[_]](policyResponders.head, req)
            generateResponse(() => (responder.apply(), responder.status,
                responder.contentType.getOrElse(ContentType.getTypeString(action.contentType))
            ))
        }
        // else execute the service call
        else {
            val service = instantiate[Service[_]](action.resolveServiceClass, req)
            val responder = instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)

            generateResponse(() => {
                // get the service result
                val serviceResult = service()

                // execute post service (pre-responder hook)
                objectify.postServiceHook(serviceResult, responder)

                // execute responder and extract status
                (responder.applyAny(serviceResult), responder.status,
                    responder.contentType.getOrElse(ContentType.getTypeString(action.contentType))
                )
            })
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

    def generateResponse[T: ClassManifest](result: () => (T, Option[Int], String)): ObjectifyResponse[T] = {
        try {
            val (content, status, contentType) = result()
            new ObjectifyResponse(contentType, status.getOrElse(200), content)
        }
        catch {
            case e: ObjectifyException => {
                throw e
            }
            case e: ObjectifyExceptionWithCause => {
                throw e
            }
            case e: Exception => {
                throw new ObjectifyExceptionWithCause(500, "Unexpected Exception", e)
            }
        }
    }
}

