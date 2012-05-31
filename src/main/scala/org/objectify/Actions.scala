package org.objectify

import org.objectify.policies.Policy
import org.objectify.services.Service
import mojolly.inflector.InflectorImports._
import resolvers.ClassResolver
import responders.{PolicyResponder, ServiceResponder}

object HttpMethod extends Enumeration {
    type HttpMethod = Value
    val Head, Get, Post, Put, Delete, Trace, Options, Connect, Patch = Value
}

import HttpMethod._

/**
  * An Objectify Action is a mapping of an HTTP Verb + URL pattern to a
  * set of policies a service and a responder
  */
case class Action(method: HttpMethod,
                  var name: String,
                  var route: Option[String] = None,
                  policies: Option[Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]] = None,
                  service: Option[Class[_ <: Service[_]]] = None,
                  responder: Option[Class[_ <: ServiceResponder[_,_]]] = None) {

    // conditionally set the route
    def setRouteIfNone(newRoute: String) = {
        if (route.isEmpty) {
            route = Some(newRoute)
        }
    }

    def resolvePolicies: Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]] = {
        policies.getOrElse(Map())
    }

    /**
      * Resolves the service class by either returning the preset service class
      * or find the class based on the name of this action
      *
      * eg: pictures index => PicturesDeleteService
      * @return
      */
    def resolveServiceClass: Class[_ <: Service[_]] = {
        service.getOrElse(ClassResolver.resolveServiceClass(getSerivceClassName(name)))
    }

    def resolveResponderClass: Class[_ <: ServiceResponder[_,_]] = {
        responder.getOrElse(ClassResolver.resolveResponderClass(getResponderClassName(name)))
    }

    private def getSerivceClassName(name: String) = {
        name + "Service"
    }

    private def getResponderClassName(name: String) = {
        name + "Responder"
    }

    override def toString = {
        val stringBuilder = new StringBuilder()
        stringBuilder.append("Actions(")
        stringBuilder.append(method)
        stringBuilder.append(',')
        stringBuilder.append(name)
        stringBuilder.append(',')
        stringBuilder.append(route.getOrElse("NO ROUTE"))
        stringBuilder.append(',')
        stringBuilder.append(resolveServiceClass)
        stringBuilder.append(")")
        stringBuilder.toString
    }

    override def equals(other: Any): Boolean = {
        if (!other.isInstanceOf[Action]) {
            return false
        }
        val otherAction = other.asInstanceOf[Action]
        this.method == otherAction.method && this.name.equals(otherAction.name)
    }

    override def hashCode() = method.hashCode() + name.hashCode
}

case class Actions() extends Iterable[Action] {

    var actions: Map[HttpMethod, Map[String, Action]] = HttpMethod.values.map(_ -> Map[String, Action]()).toMap

    /**
      * Default routing configuration point assumes to create an
      * policy free (public) set of routes that map to the
      * following services
      *
      * GET 	/#{name} 		#{name}IndexService
      * GET 	/#{name}/:id 	#{name}ShowService
      * GET 	/#{name}/new 	#{name}NewService
      * POST 	/#{name} 		#{name}CreationService
      * GET 	/#{name}/edit 	#{name}EditService
      * PUT 	/#{name}/:id	#{name}UpdateService
      * DELETE 	/#{name}/:id 	#{name}DestructionService
      */

    def resource(name: String,
                 index: Option[Action] = Some(Action(Get, "index")),
                 show: Option[Action] = Some(Action(Get, "show")),
                 `new`: Option[Action] = Some(Action(Get, "new")),
                 create: Option[Action] = Some(Action(Post, "create")),
                 edit: Option[Action] = Some(Action(Get, "edit")),
                 update: Option[Action] = Some(Action(Put, "update")),
                 destroy: Option[Action] = Some(Action(Delete, "destroy"))) {

        // update the routes if they haven't been set
        val route = name.pluralize

        // Ensure that all the actions have resty routes.
        resolveRouteAndName(index, route, route)
        resolveRouteAndName(show, route, route + "/:id")
        resolveRouteAndName(`new`, route, route + "/new")
        resolveRouteAndName(create, route, route)
        resolveRouteAndName(edit, route, route + "/:id/edit")
        resolveRouteAndName(update, route, route + "/:id")
        resolveRouteAndName(destroy, route, route + "/:id")
    }

    private def resolveRouteAndName(actionOption: Option[Action], namePrefix: String, route: String) {
        actionOption.map(a => {
            a.name = namePrefix.capitalize + a.name.capitalize
            a.setRouteIfNone(route)
            action(a)
        })
    }

    def action(action: Action) {
        var map = actions(action.method)
        map += (action.route.get -> action)
        actions += (action.method -> map)
        action
    }

    override def iterator = {
        actions.values.flatMap(_.values).iterator
    }

    def bootstrapValidation() {
        for {
            (verb, actionsEntry) <- actions
            (string, action) <- actionsEntry
        } {
            action.resolvePolicies
            action.resolveServiceClass
            action.resolveResponderClass
        }
    }

    //    // http verbs for configruration
    //    def head(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(HEAD, route, policies, service, responder)
    //    }
    //
    //    def get(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(GET, route, policies, service, responder)
    //    }
    //
    //    def post(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(POST, route, policies, service, responder)
    //    }
    //
    //    def put(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(PUT, route, policies, service, responder)
    //    }
    //
    //    def delete(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(DELETE, route, policies, service, responder)
    //    }
    //
    //    def trace(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(TRACE, route, policies, service, responder)
    //    }
    //
    //    def options(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(OPTIONS, route, policies, service, responder)
    //    }
    //
    //    def connect(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(CONNECT, route, policies, service, responder)
    //    }
    //
    //    def patch(route: String, policies: List[Class[Policy]], service: Class[Service], responder: Class[Responder]) = {
    //        action(PATCH, route, policies, service, responder)
    //    }

    override def toString() = {
        val stringBuilder = new StringBuilder()
        stringBuilder.append("\n")
        stringBuilder.append("Actions[")
        actions.foreach {
            case (verb, actions) => {
                stringBuilder.append("\n\t")
                stringBuilder.append(verb)
                actions.foreach {
                    case (route, action) => {
                        stringBuilder.append("\n\t\t")
                        stringBuilder.append(action)
                    }
                }
            }
        }
        stringBuilder.append("\n")
        stringBuilder.append("]")
        stringBuilder.toString
    }
}