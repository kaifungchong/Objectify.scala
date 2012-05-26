package org.objectify.executor

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.objectify.{Action, Objectify}
import org.scalatra.SinatraPathPatternParser
import org.objectify.policies.Policy
import org.objectify.responders.Responder
import org.objectify.services.Service


/**
 * This class is responsible for executing the pipeline for the lifecycle of a request.
 *
 * @author Arthur Gonigberg
 * @since 12-05-24
 */
class ObjectifyPipeline(objectify: Objectify) {

  def handleRequest(req: HttpServletRequest, resp: HttpServletResponse) {
    // find correct route based on path of request
    val actionResource = matchUrlToRoute(req.getContextPath + req.getServletPath + req.getPathInfo, objectify.actions.routeRegistry)
      .getOrElse(throw new IllegalArgumentException("Could not determine the route to use"))

    // todo figure out paramter resolver here
    // execute policies
    val policies = Invoker[Policy]().invoke(actionResource.resolvePolicies)
    val policyResponder:Option[Responder] = (for {policy <- policies if !policy.isAllowed} yield policy.getResponder).headOption

    // if policies failed respond with first failure
    if (policyResponder.isDefined) {
      populateResponse( policyResponder.get, resp )
    }
    // else execute the service call
    else {
      // todo do something useful here
      val service = Invoker[Service]().invoke(actionResource.resolveServiceClass)
      val serviceResult = service.apply()

      val responder = Invoker[Responder]().invoke(actionResource.resolveResponderClass)
    }

  }

  private[executor] def populateResponse(responder:Responder, response: HttpServletResponse) {
    // todo serialization plugins
    response.getWriter.println(responder())
  }

  private[executor] def matchUrlToRoute(path: String, actionMap: Map[String, Action]): Option[Action] = {
    var route = actionMap.filter(entry => RouteMatcher.isMatched("/" + entry._1, path))

    // this is not good.. attempt exact match
    /*
      Weird edge case: path/:id == path/keyword in regex
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


