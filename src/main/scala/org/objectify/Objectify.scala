/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify

import com.twitter.logging.Logger
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.executor.{ObjectifyPipeline, ObjectifyResponse}
import org.objectify.responders.ServiceResponder

/**
 * Main Objectify object for extending with your framework of choice
 */
case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions())
  extends ObjectifySugar with ObjectifyImplicits {
  private val logger = Logger(classOf[Objectify])
  var postServiceHook = (serviceResult: Any, responder: ServiceResponder[_, _]) => {}

  override def toString = {
    "Objectify Configuration" + actions.toString
  }

  def bootstrap() {
    actions.bootstrapValidation()
  }

  /**
   * Execute the request through the pipeline
   * @param action - the Action to execute
   * @param requestAdapter - the adapter to handle the request
   * @return - the response wrapped in an ObjectifyResponse
   */
  def execute(action: Action, requestAdapter: ObjectifyRequestAdapter): ObjectifyResponse[_] = {
    val pipeline = new ObjectifyPipeline(this)
    val start = System.currentTimeMillis()
    try {
      pipeline.handleRequest(action, requestAdapter)
    }
    finally {
      // want to get a logging statement even if request throws exception
      val requestTime = System.currentTimeMillis() - start

      val queryString = Option(requestAdapter.getRequest.getQueryString)

      val path = s"${requestAdapter.getPath}${queryString.map(s => s"?$s").getOrElse("")}"

      logger.info(s"Request ${requestAdapter.getHttpMethod.toString.toUpperCase}: $path took $requestTime ms for action ${action.name}.")
    }
  }
}
