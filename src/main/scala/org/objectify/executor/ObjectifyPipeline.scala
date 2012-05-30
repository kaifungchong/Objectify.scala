package org.objectify.executor

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.scalatra.SinatraPathPatternParser
import org.objectify.policies.Policy
import org.objectify.responders.Responder
import org.objectify.{Action, Objectify}


/**
 * This class is responsible for executing the pipeline for the lifecycle of a request.
 *
 * @author Arthur Gonigberg
 * @since 12-05-24
 */
class ObjectifyPipeline(objectify: Objectify) {

  def handleRequest(req: HttpServletRequest, resp: HttpServletResponse) {
    // find routes based on verb
    val routes: Map[String, Action] = objectify.actions.actions.find(entry => entry._1.toString.equalsIgnoreCase(req.getMethod))
      .getOrElse(throw new IllegalArgumentException("Could not determine the verb."))._2

    // find correct route based on path of request
    val actionResource = matchUrlToRoute(req.getContextPath + req.getServletPath + req.getPathInfo, routes)
      .getOrElse(throw new IllegalArgumentException("Could not determine the route to use"))

    // execute policies
    val policyResponders =
      for {(policy, responder) <- actionResource.resolvePolicies
           if (!instantiate[Policy](policy, req).isAllowed)}
      yield responder

    // if policies failed respond with first failure
    if (!policyResponders.isEmpty) {
      populateResponse(instantiate[Responder](policyResponders.head, req), resp)
    }
    // else execute the service call
    else {
      // todo do something useful here
    }

  }

  private def instantiate[T: ClassManifest](klass: Class[_ <: T], req: HttpServletRequest) = {
    Invoker.invoke(klass, req)
  }

  private[executor] def populateResponse(responder: Responder, response: HttpServletResponse) {
    response.getWriter.println(responder(None))
  }

  private[executor] def matchUrlToRoute(path: String, actionMap: Map[String, Action]): Option[Action] = {
    var route = actionMap.filter(entry => RouteMatcher.isMatched("/" + entry._1, path))

    /*
      Weird edge case: path/:id == path/keyword in regex
      This is not good.. attempt exact match
     */
    if (route.size != 1) {
      // TODO make this less shitty
      route = route.filter(entry => ("/" + entry._1).equals(path))

      if (route.size != 1) {
        throw new IllegalStateException("Could not map route due to ambiguous route definition.")
      }
    }

    Option(route.headOption.get._2)
  }
}

private object RouteMatcher {
  def isMatched(pattern: String, path: String) = {
    // using Scalatra's implementation so we can use the same keywords in our route definition
    SinatraPathPatternParser(pattern)(path).isDefined
  }
}


