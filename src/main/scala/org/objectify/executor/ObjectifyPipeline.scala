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
import org.objectify.HttpStatus.HttpStatus
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.{ObjectifyException, ObjectifyExceptionWithCause}
import org.objectify.policies.Policy
import org.objectify.responders.{PolicyResponder, ResponderResult, ServiceResponder}
import org.objectify.services.{RedirectResult, Service}
import org.objectify.{HttpStatus, Action, Objectify}

import scala.reflect.ClassTag

/**
 * This class is responsible for executing the pipeline for the lifecycle of a request.
 */
class ObjectifyPipeline(objectify: Objectify) {

  private val logger = Logger(classOf[ObjectifyPipeline])

  def handleRequest(action: Action, req: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
    // determine which policies to execute -- globals + action defaults or globals + action overrides
    val policiesToExecute = getPolicies(action)

    // execute
    logger.debug("Executing Policies")
    val policyResponders = {
      for {(policy, responder) <- policiesToExecute
           if !instantiate[Policy](policy, req).isAllowed
      } yield (policy, responder)
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

      logger.trace("Instantiating Service Class")
      val service = instantiate[Service[_]](serviceClass, req)
      logger.trace("Done")

      val responder = instantiate[ServiceResponder[_, _]](action.resolveResponderClass, req)

      logger.trace(s"Executing service $serviceClass")
      // get the service result
      val serviceResult = service()

      logger.trace(s"Result $serviceResult")

      // execute post service (pre-responder hook)
      objectify.postServiceHook(serviceResult, responder)

      // execute responder and extract status
      // TODO: make the headers story stronger
      val (headers, result, status, contentType) = responder.applyAny(serviceResult) match {
        case responderResult: ResponderResult => (responder.headers ++ responderResult.headers, responderResult.value, responderResult.httpStatus, responder.contentType)
        case a: Any => (Map[String, String](), a, responder.status, responder.contentType)
      }

      generateResponse(result, status, contentType, headers)
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
    logger.trace(s"Instantiating Class: ${klass.getSimpleName}")
    val result = Invoker.invoke(klass, req)
    logger.trace("Finished Instantiating Class: " + result)

    result
  }

  def generateResponse[T: ClassTag](content: T, status: HttpStatus, contentType: ContentType, headers: Map[String, String] = Map.empty): ObjectifyResponse[T] = {
    try {
      new ObjectifyResponse(contentType, status, content, headers)
    }
    catch {
      case e: ObjectifyException =>
        e.printStackTrace()
        throw e
      case e: ObjectifyExceptionWithCause =>
        e.printStackTrace()
        throw e
      case e: Throwable =>
        e.printStackTrace()
        throw new ObjectifyExceptionWithCause(500, "Unexpected Exception", e)
    }
  }
}

