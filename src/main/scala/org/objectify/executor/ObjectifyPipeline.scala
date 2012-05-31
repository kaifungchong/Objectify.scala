package org.objectify.executor

import org.scalatra.SinatraPathPatternParser
import org.objectify.policies.Policy
import org.objectify.{Action, Objectify}
import org.objectify.services.Service
import org.objectify.responders.{PolicyResponder, ServiceResponder}

/**
  * This class is responsible for executing the pipeline for the lifecycle of a request.
  */
class ObjectifyPipeline(objectify: Objectify) {

    def handleRequest(req: ObjectifyRequest): ObjectifyResponse[_] = {
        // find routes based on verb
        val routes: Map[String, Action] = objectify.actions.actions.find(entry => entry._1.equals(req.getHttpMethod))
            .getOrElse(throw new IllegalArgumentException("Could not determine the verb."))._2

        // find correct route based on path of request
        val actionResource = matchUrlToRoute(req.getPath, routes)
            .getOrElse(throw new IllegalArgumentException("Could not determine the route to use"))

        // execute policies
        val policyResponders =
            for {(policy, responder) <- actionResource.resolvePolicies
                 if (!instantiate[Policy](policy, req).isAllowed)} yield responder

        // if policies failed respond with first failure
        if (policyResponders.nonEmpty) {
            ResponseGenerator.generateResponse(instantiate[PolicyResponder[_]](policyResponders.head, req).apply())
        }
        // else execute the service call
        else {
            val service = instantiate[Service[_]](actionResource.resolveServiceClass, req)
            val responder = instantiate[ServiceResponder[_, _]](actionResource.resolveResponderClass, req)

            ResponseGenerator.generateResponse(responder.applyAny(service()))
        }
    }

    private def instantiate[T: ClassManifest](klass: Class[_ <: T], req: ObjectifyRequest) = {
        Invoker.invoke(klass, req)
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

private object ResponseGenerator {
    def generateResponse(result: Any): ObjectifyResponse[_] = {
        new ObjectifyResponse("text/plain", 200, result)
    }
}

