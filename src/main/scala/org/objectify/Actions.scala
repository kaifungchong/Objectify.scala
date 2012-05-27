package org.objectify

import org.objectify.policies.Policy
import org.objectify.services.Service
import mojolly.inflector.InflectorImports._
import resolvers.ClassResolver
import responders.Responder

object Verb extends Enumeration {
  type Verb = Value
  val HEAD, GET, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH = Value
}

import Verb._

/**
 * An Objectify Action is a mapping of an HTTP Verb + URL pattern to a
 * set of policies a service and a responder
 */
case class Action(verb: Verb,
                  var name: String,
                  var route: Option[String] = None,
                  policies: Option[List[Class[_ <: Policy]]] = None,
                  service: Option[Class[_ <: Service]] = None,
                  responder: Option[Class[_ <: Responder]] = None) {

  // conditionally set the route
  def setRouteIfNone(newRoute: String) = {
    if (route.isEmpty) {
      route = Some(newRoute)
    }
  }

  def resolvePolicies: List[Class[_ <: Policy]] = {
    policies.getOrElse(Nil)
  }


  /**
   * Resolves the service class by either returning the preset service class
   * or find the class based on the name of this action
   *
   * eg: pictures index => PicturesIndexService
   * @return
   */
  def resolveServiceClass: Class[_ <: Service] = {
    service.getOrElse(ClassResolver.resolveServiceClass(getSerivceClassName(name)))
  }

  def resolveResponderClass: Class[_ <: Responder] = {
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
    stringBuilder.append(verb)
    stringBuilder.append(',')
    stringBuilder.append(name)
    stringBuilder.append(',')
    stringBuilder.append(route.getOrElse("NO ROUTE"))
    stringBuilder.append(',')
    stringBuilder.append(resolveServiceClass)
    stringBuilder.append(")")
    stringBuilder.toString
  }

  override def equals(other: Any):Boolean = {
    if ( !other.isInstanceOf[Action]) {
      return false
    }
    val otherAction = other.asInstanceOf[Action]
    this.verb == otherAction.verb && this.name.equals(otherAction.name)
  }

  override def hashCode() = verb.hashCode() + name.hashCode
}

case class Actions() {

  var actions: Map[Verb, Map[String, Action]] = Verb.values.map(_ -> Map[String, Action]()).toMap

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
               index: Option[Action] = Some(Action(GET, "index")),
               show: Option[Action] = Some(Action(GET, "show")),
               `new`: Option[Action] = Some(Action(GET, "new")),
               create: Option[Action] = Some(Action(POST, "create")),
               edit: Option[Action] = Some(Action(GET, "edit")),
               update: Option[Action] = Some(Action(PUT, "update")),
               destroy: Option[Action] = Some(Action(DELETE, "destroy"))) {

    // update the routes if they haven't been set
    val route = name.pluralize

    // Ensure that all the actions have resty routes.
    setRouteAndNameAndAdd(index, route, route)
    setRouteAndNameAndAdd(show, route, route + "/:id")
    setRouteAndNameAndAdd(`new`, route, route + "/new")
    setRouteAndNameAndAdd(create, route, route)
    setRouteAndNameAndAdd(edit, route, route + "/:id/edit")
    setRouteAndNameAndAdd(update, route, route + "/:id")
    setRouteAndNameAndAdd(destroy, route, route + "/:id")
  }

  private def setRouteAndNameAndAdd(actionOption: Option[Action], namePrefix: String, route: String) {
    actionOption.map(a => {
      a.name = namePrefix.capitalize + a.name.capitalize
      a.setRouteIfNone(route)
      action(a)
    })
  }

  def action(action: Action) {
    var map = actions(action.verb)
    map += (action.route.get -> action)
    actions += (action.verb -> map)
    action
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