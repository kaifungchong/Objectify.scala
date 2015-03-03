/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import com.twitter.logging.Logger
import org.objectify.ContentType.ContentType
import org.objectify.HttpStatus.HttpStatus
import org.objectify.policies.Policy
import org.objectify.services.Service
import org.objectify.responders.{ResponderResult, PolicyResponder, ServiceResponder}
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.{ObjectifyExceptionWithCause, ObjectifyException}
import org.objectify.{Action, Objectify}
import scala.reflect.ClassTag

import reflect.runtime.universe._
import scala.reflect.api.Universe

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
           if !instantiate[Policy](policy, req).isAllowed} yield (policy, responder)
    }


    // if policies failed respond with first failure
    if (policyResponders.nonEmpty) {
      // instantiate responder
      val (policyClass, responderClass) = policyResponders.head
      val responder = instantiate[PolicyResponder[_]](responderClass, req)
      responder.policy = Some(policyClass)

      // generate response
      generateResponse(responder.apply(), responder.status, responder.contentType)
    }
    // else execute the service call
    else {

      val serviceClass = action.resolveServiceClass

//      val typeConstructor = typeOf[serviceClass.type].typeConstructor
//
//      logger.info("Trying to spew out param names for : " + serviceClass)
//
//      val m = runtimeMirror(getClass.getClassLoader)
//      val cm = m.reflect(serviceClass)
//
//      typeConstructor.members.filter(!_.isMethod).foreach(param => {
//        logger.info(param.name.toString)
//      })

      val service = instantiate[Service[_]](action.resolveServiceClass, req)

      val responder = try {
        instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)
      }
      catch {
        case e: Throwable => {
          e.printStackTrace()
          instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)
        }
      }

      // get the service result
      val serviceResult = service(

      )

      // execute post service (pre-responder hook)
      objectify.postServiceHook(serviceResult, responder)

      // execute responder and extract status
      val (result, status, contentType) = responder.applyAny(serviceResult) match {
        case responderResult: ResponderResult => (responderResult.value, responderResult.httpStatus, responder.contentType)
        case a: Any => (a, responder.status, responder.contentType)
      }


      generateResponse(result, status, contentType)
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

  private def instantiate[T: ClassTag](klass: Class[_ <: T], req: ObjectifyRequestAdapter): T = {
    Invoker.invoke(klass, req)
  }

  def generateResponse[T: ClassTag](content: T, status: HttpStatus, contentType: ContentType): ObjectifyResponse[T] = {
    try {
      new ObjectifyResponse(contentType, status, content)
    }
    catch {
      case e: ObjectifyException => {
        e.printStackTrace()
        throw e
      }
      case e: ObjectifyExceptionWithCause => {
        e.printStackTrace()
        throw e
      }
      case e: Throwable => {
        e.printStackTrace()
        throw new ObjectifyExceptionWithCause(500, "Unexpected Exception", e)
      }
    }
  }
}

