/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify

import mojolly.inflector.Inflector
import mojolly.inflector.InflectorImports._
import org.objectify.exceptions.ConfigurationException
import org.objectify.policies.Policy
import org.objectify.resolvers.ClassResolver
import org.objectify.responders.{GenericResponder, PolicyResponder, ServiceResponder}
import org.objectify.services.Service

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val Head, Get, Post, Put, Delete, Trace, Options, Connect, Patch = Value
}

object ContentType extends Enumeration {
  type ContentType = Value

  val HTML = Value("text/html")
  val JSON = Value("application/json")
  val XML = Value("application/xml")
  val TEXT = Value("text/plain")
  val CSV = Value("text/csv")
  val PDF = Value("application/pdf")
}

object HttpStatus extends Enumeration {
  type HttpStatus = Value

  val NotSet = Value(0);
  val Continue = Value(100)
  val SwitchingProtocols = Value(101)
  val Processing = Value(102)

  val Ok = Value(200)
  val Created = Value(201)
  val Accepted = Value(202)
  val NonAuthoritativeInformation = Value(203)
  val NoContent = Value(204)
  val ResetContent = Value(205)
  val PartialContent = Value(206)
  val MultiStatus = Value(207)

  val MultipleChoices = Value(300)
  val MovedPermanently = Value(301)
  val Found = Value(302)
  val SeeOther = Value(303)
  val NotModified = Value(304)
  val UseProxy = Value(305)
  val TemporaryRedirect = Value(307)

  val BadRequest = Value(400)
  val Unauthorized = Value(401)
  val PaymentRequired = Value(402)
  val Forbidden = Value(403)
  val NotFound = Value(404)
  val MethodNotAllowed = Value(405)
  val NotAcceptable = Value(406)
  val ProxyAuthenticationRequired = Value(407)
  val RequestTimeout = Value(408)
  val Conflict = Value(409)
  val Gone = Value(410)
  val LengthRequired = Value(411)
  val PreconditionFailed = Value(412)
  val RequestEntityTooLarge = Value(413)
  val RequestUriTooLong = Value(414)
  val UnsupportedMediaType = Value(415)
  val RequestedRangeNot_Satisfiable = Value(416)
  val ExpectationFailed = Value(417)
  val EnhanceYourCalm = Value(420)
  val UnprocessableEntity = Value(422)
  val Locked = Value(423)
  val FailedDependency = Value(424)

  val InternalServerError = Value(500)
  val NotImplemented = Value(501)
  val BadGateway = Value(502)
  val ServiceUnavailable = Value(503)
  val GatewayTimeout = Value(504)
  val HttpVersionNotSupported = Value(505)
  val InsufficientStorage = Value(507)
}

case class AcceptType(content: Option[ContentType.ContentType])

import org.objectify.ContentType._
import org.objectify.HttpMethod._

/**
 * An Objectify Action is a mapping of an HTTP Verb + URL pattern to a
 * set of policies a service and a responder
 */
case class Action(method: HttpMethod,
                  var name: String,
                  var route: Option[String] = None,
                  var policies: Option[Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]] = None,
                  var service: Option[Class[_ <: Service[_]]] = None,
                  var responder: Option[Class[_ <: ServiceResponder[_, _]]] = None,
                  var ignoreGlobalPolicies: Boolean = false,
                  var resource: Option[String] = None) {

  // conditionally set the route
  def setRouteIfNone(newRoute: String) {
    if (route.isEmpty) {
      route = Some(newRoute)
    }
  }

  def resolvePolicies: Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]] = {
    policies.getOrElse(Map())
  }

  /**
   * Resolves the responder class by either returning the preset service class
   * or finding the class based on the name of this action if that fails we get
   * return the generic one.
   *
   * eg: pictures index => PicturesIndexResponder
   */
  def resolveResponderClass: Class[_ <: ServiceResponder[_, _]] = {
    responder
      .getOrElse(
        ClassResolver.resolveResponderClassOption(getResponderClassName(name))
          .getOrElse(classOf[GenericResponder])
      )
  }

  private def getResponderClassName(name: String) = {
    name + "Responder"
  }

  override def toString = {
    s"Action($method, ${route.getOrElse("NO ROUTE")}, ${resolveServiceClass.getSimpleName}})"
  }

  /**
   * Resolves the service class by either returning the preset service class
   * or finding the class based on the name of this action
   *
   * eg: pictures index => PicturesIndexService
   */
  def resolveServiceClass: Class[_ <: Service[_]] = {
    service.getOrElse(ClassResolver.resolveServiceClass(getSerivceClassName(name)))
  }

  /**
   * Convention over configuration
   */
  private def getSerivceClassName(name: String) = {
    name + "Service"
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

/**
 * Implicit conversion to policy tuples make route definition much simpler and prettier
 */
class PolicyTuple(val tuple: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) {
  var onlyAct: List[Option[Action]] = Nil
  var exceptAct: List[Option[Action]] = Nil
  var onlyStr: List[String] = Nil
  var exceptStr: List[String] = Nil

  def onlyActions(actions: Action*) = {
    onlyAct = actions.toList.map(Some(_))
    this
  }

  def exceptActions(actions: Action*) = {
    exceptAct = actions.toList.map(Some(_))
    this
  }

  def only(actions: String*) = {
    onlyStr = actions.toList
    this
  }

  def except(actions: String*) = {
    exceptStr = actions.toList
    this
  }
}

case class Actions() extends Iterable[Action] {

  var actions: Map[HttpMethod, Map[String, Action]] = HttpMethod.values.toList.map(v => v -> Map[String, Action]()).toMap

  def actionBase(httpMethod: HttpMethod, name: String, route: String, contentType: ContentType = JSON,
                 policies: Option[Map[Class[_ <: Policy], Class[_ <: PolicyResponder[_]]]] = None,
                 service: Option[Class[_ <: Service[_]]] = None, responder: Option[Class[_ <: ServiceResponder[_, _]]] = None,
                 ignoreGlobalPolicies: Boolean = false) {
    val action = Action(httpMethod, name, Some(route), policies, service, responder, ignoreGlobalPolicies)

    resolveRouteAndName(Some(action), "", route)
  }

  def singularResource(name: String) = resource(name, pluralize = false)

  def simpleResource(name: String, actionTuple: (String, String)) = resource(name, pluralize = false) onlyRoute actionTuple

  /**
   * Default routing configuration point assumes to create an
   * policy free (public) set of routes that map to the
   * following services
   *
   * GET 	/#{name} 		#{name}IndexService
   * GET 	/#{name}/:id 	#{name}ShowService
   * POST 	/#{name} 		#{name}CreationService
   * PUT 	/#{name}/:id	#{name}UpdateService
   * DELETE 	/#{name}/:id 	#{name}DestructionService
   */
  def resource(name: String,
               index: Option[Action] = Some(Action(Get, "index")),
               show: Option[Action] = Some(Action(Get, "show")),
               create: Option[Action] = Some(Action(Post, "create")),
               update: Option[Action] = Some(Action(Put, "update")),
               destroy: Option[Action] = Some(Action(Delete, "destroy")),
               pluralize: Boolean = true): Resource = {

    // update the routes if they haven't been set
    val route = if (pluralize) name.pluralize else name

    // Ensure that all the actions have resty routes.
    resolveRouteAndName(index, route, route)
    resolveRouteAndName(show, route, route + "/:id")
    resolveRouteAndName(create, route, route)
    resolveRouteAndName(update, route, route + "/:id")
    resolveRouteAndName(destroy, route, route + "/:id")

    new Resource(List(index, show, create, update, destroy))
  }

  private def resolveRouteAndName(actionOption: Option[Action], namePrefix: String, route: String, resource: Option[String] = None) {
    actionOption.map(a => {
      a.name = namePrefix.capitalize + a.name.capitalize
      a.resource = Some(resource.getOrElse(namePrefix))
      a.setRouteIfNone(route)
      action(a)
    })
  }

  private def action(action: Action) = {
    var map = actions(action.method)
    map += (action.route.get -> action)
    actions += (action.method -> map)

    action
  }

  def simpleResource(name: String, verb: String) = resource(name, pluralize = false) only verb

  def action(name: String, verb: HttpMethod = Get, routeOverride: Option[String] = None) = new Resource(Nil) action(name, verb, None, None, routeOverride)

  def removeActions(actionsRemove: List[Action]) {
    for (action <- actionsRemove) {
      var map = actions(action.method)
      map = map.filterNot(route => action.route.get.equals(route._1) && action == route._2)
      actions += (action.method -> map)
    }
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
      val service = action.resolveServiceClass
      val responder = action.resolveResponderClass

      // tolerate generic responder
      if (responder != classOf[GenericResponder]) {
        // make sure service and resolver are compatible
        val returnType = service.getMethod("apply").getReturnType
        try {
          responder.getMethod("apply", returnType).getParameterTypes.head
        }
        catch {
          case e: NoSuchMethodException =>
            e.printStackTrace()
            val paramType = responder.getMethods.find(method => {
              method.getName.startsWith("apply") && method.getParameterTypes.head != classOf[Object]
            }) match {
              case Some(method) => Inflector.titleize(method.getParameterTypes.headOption.map(_.getSimpleName).getOrElse("Undefined"))
              case None => "Undefined"
            }

            throw new ConfigurationException(s"Service [${service.getSimpleName}] and Responder [${responder.getSimpleName}] are not compatible. Service return type [${returnType.getSimpleName}] does not match Responder apply method parameter [$paramType].")
        }
      }
    }
  }

  override def toString() = {
    val stringBuilder = new StringBuilder()
    stringBuilder.append("\n")
    stringBuilder.append("Actions[")
    actions.foreach {
      case (verb, innerActions) =>
        stringBuilder.append("\n\t")
        stringBuilder.append(verb)
        innerActions.foreach {
          case (route, action) =>
            stringBuilder.append("\n\t\t")
            stringBuilder.append(action)
        }
    }
    stringBuilder.append("\n")
    stringBuilder.append("]")
    stringBuilder.toString()
  }

  /**
   * This class is mainly here to help in creating a pretty syntax with chained calls
   */
  class Resource(private var actions: List[Option[Action]]) {

    def action(route: String, verb: HttpMethod = Get,
               namePrefix: Option[String] = actions.head.map(_.resource.getOrElse("")),
               routePrefix: Option[String] = actions.find(_.get.route.get.endsWith(":id")).map(_.get.route.getOrElse("")),
               routeOverride: Option[String] = None,
               isIndex: Boolean = false) = {

      val action = Some(Action(verb, if (isIndex && verb.equals(Get)) "index" else verb.toString.toLowerCase))

      // index paths -- e.g. /courses/grouped -> CoursesGroupedGet
      if (isIndex) {
        val _name = namePrefix.getOrElse("") + route.capitalize
        val _routePrefix = Some(namePrefix.getOrElse(""))
        val _route = _routePrefix.map(_ + "/").getOrElse("") + routeOverride.getOrElse(route)
        resolveRouteAndName(action, _name, _route, namePrefix)
      }
      // show paths (default) -- e.g. /courses/:id/duplicate -> CourseDuplicatePost
      else {
        val _name = namePrefix.map(_.singularize).getOrElse("") + route.capitalize
        val _route = routePrefix.map(_ + "/").getOrElse("") + routeOverride.getOrElse(route)
        resolveRouteAndName(action, _name, _route, namePrefix)
      }

      actions = action :: actions

      ResourceAction(this, action.get)
    }

    def onlyRoute(actionTuples: (String, String)*): Resource = {
      onlyRouteWithService(actionTuples.map(a => ((a._1, a._2), (None, None))): _*)
    }

    def onlyRouteWithService(actionTuples: ((String, String),
      // option service -> service responder for explicit overrides
      (Option[Class[_ <: Service[_]]], Option[Class[_ <: ServiceResponder[_, _]]]))*): Resource = {
      val actionStrings = actionTuples.map(_._1._1)
      only(actionStrings: _*)

      for {((action, route), serviceResponderMapping) <- actionTuples} {
        val a = string2Actions(Seq(action)).headOption
        if (a.isDefined && a.get.isDefined) {
          if (serviceResponderMapping._1.isDefined) a.get.get.service = serviceResponderMapping._1
          if (serviceResponderMapping._2.isDefined) a.get.get.responder = serviceResponderMapping._2

          a.get.get.route = Some(route)
        }
      }

      this
    }

    def only(actionStrings: String*): Resource = {
      val actualActions = string2Actions(actionStrings)
      val actionsToRemove = actions.filterNot(actualActions.contains(_))
      removeActions(actionsToRemove.map(_.get))
      this
    }

    // try to match up name with reverse of resolveRouteAndName
    private def string2Actions(actionStr: Seq[String]) = {
      actions.filter(action => {
        actionStr.count(s => action.get.name.toLowerCase.endsWith(s)) > 0
      })
    }

    def onlyRouteWithPrefix(prefix: String, actionTuples: (String, String)*): Resource = {
      onlyRouteWithService(actionTuples.map(a => ((a._1, prefix + "/" + a._2), (None, None))): _*)
    }

    def except(actionStrings: String*): Resource = {
      val actualActions = string2Actions(actionStrings)
      val actionsToRemove = actions.filter(actualActions.contains(_))
      removeActions(actionsToRemove.map(_.get))
      this
    }

    def policy(policy: PolicyTuple): Resource = {
      val applyActions = getActionsFromPolicyTuple(policy)
      applyPolicies(applyActions, policy.tuple)
      this
    }

    private def getActionsFromPolicyTuple(tuple: PolicyTuple): List[Option[Action]] = {
      // either only or except -- not both
      if (tuple.onlyStr.nonEmpty || tuple.onlyAct.nonEmpty) {
        string2Actions(tuple.onlyStr) ++ tuple.onlyAct
      }
      else if (tuple.exceptStr.nonEmpty || tuple.exceptAct.nonEmpty) {
        actions.filterNot({
          (string2Actions(tuple.exceptStr) ++ tuple.exceptAct).contains(_)
        })
      }
      else {
        actions
      }
    }

    // apply a single policy to any number of actions
    private def applyPolicies(actions: List[Option[Action]], policy: (Class[_ <: Policy], Class[_ <: PolicyResponder[_]])) {
      actions.flatten.foreach(action => {
        val actionPols = action.policies
        val allPolicies = if (actionPols.isDefined) actionPols.get ++ Map(policy) else Map(policy)
        action.policies = Some(allPolicies)
      })
    }

    def policies(policies: PolicyTuple*): Resource = {
      for (policy <- policies) {
        val applyActions = getActionsFromPolicyTuple(policy)
        applyPolicies(applyActions, policy.tuple)
      }
      this
    }

    def ignoreGlobalPolicies(): Resource = {
      for (action <- actions) {
        action.get.ignoreGlobalPolicies = true
      }
      this
    }

    def ignoreGlobalPoliciesOnlyActions(onlyActions: Action*): Resource = {
      for (action <- onlyActions) {
        action.ignoreGlobalPolicies = true
      }

      this
    }

    def ignoreGlobalPoliciesOnly(actionStrings: String*): Resource = {
      val actualActions = string2Actions(actionStrings)
      for (action <- actualActions) {
        action.get.ignoreGlobalPolicies = true
      }

      this
    }

    case class ResourceAction(resource: Resource, action: Action) {
      def policy(policy: PolicyTuple) = {
        resource.policy(policy onlyActions action)
        this
      }

      def policies(policies: PolicyTuple*) = {
        resource.policies(policies.map(_ onlyActions action): _*)
        this
      }

      def ignoreGlobalPolicies() = {
        resource.ignoreGlobalPoliciesOnlyActions(action)
        this
      }
    }

  }

}