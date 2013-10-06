/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.objectify.HttpMethod.Delete
import org.objectify.HttpMethod.Get
import org.objectify.HttpMethod.Options
import org.objectify.HttpMethod.Patch
import org.objectify.HttpMethod.Post
import org.objectify.HttpMethod.Put
import org.objectify.{Action, Objectify}
import org.scalatra.servlet.{RichResponse, RichRequest, ServletBase}
import org.objectify.exceptions.{ObjectifyExceptionWithCause, ObjectifyException, BadRequestException}
import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.{AsyncResult, FutureSupport}
import akka.actor.{Status, Props, Actor, ActorSystem}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import akka.util.Timeout
import org.apache.commons.fileupload.FileItem
import akka.routing.{DefaultResizer, RoundRobinRouter}

trait ObjectifyScalatraAdapter extends Objectify with ServletBase with FileUploadSupport with FutureSupport {

    def actorSystem: ActorSystem

    /**
     * Decorates the default bootstrap which has the configuration
     * validation in it
     */
    override def bootstrap() {
        super.bootstrap()

        /**
         * Sort wildcards to the top so that there are no route conflicts and that routes are always
         * added consistently.
         */
        val sortedActions = actions.toList.sortBy(_.route.getOrElse(""))(new Ordering[String] {
            def compare(x: String, y: String) = {
                val wildcardSymbol = ':'
                if (x.contains(wildcardSymbol) && y.contains(wildcardSymbol)) {
                    0
                }
                else if (x.contains(wildcardSymbol)) {
                    -1
                }
                else if (y.contains(wildcardSymbol)) {
                    1
                }
                else {
                    0
                }
            }
        })

        /**
         * For each action we map to the Scalatra equivalent block parameter
         * though we are still bound to the HttpServletRequest and Response
         * currently.
         */
        val objectifyActor = actorSystem.actorOf(Props(new ObjectifyActor())
            .withRouter(RoundRobinRouter(resizer = Some(DefaultResizer(50, 1000)))))


        sortedActions.foreach(action => {
            def scalatraFunction(route: String) = action.method match {
                case Options => options(route) _
                case Get => get(route) _
                case Post => post(route) _
                case Put => put(route) _
                case Delete => delete(route) _
                case Patch => patch(route) _
            }

            scalatraFunction("/" + action.route.getOrElse(throw new BadRequestException("No Route Found"))) {
                new AsyncResult {
                    val is = {
                        import _root_.akka.pattern.ask
                        implicit val timeout = Timeout(10000)

                        objectifyActor ? (action, request, response, params(request).toMap, fileParams)
                    }
                }
            }
        })
    }

    class ObjectifyActor extends Actor {
        def receive = {
            case (action: Action, request: HttpServletRequest, response: HttpServletResponse,
                params: Map[String, String], fileParams: collection.Map[String, FileItem]) => {
                try {
                    // wrap HttpServletRequest in adapter and get ObjectifyResponse
                    val objectifyResponse = execute(action,
                        new ScalatraRequestAdapter(RichRequest(request), RichResponse(response), params, Some(fileParams)))

                    // find appropriate response adapter and serialize the response
                    sender ! locateResponseAdapter(objectifyResponse).serializeResponseAny(request, response, objectifyResponse)
                }
                catch {
                    case e: Exception => sender ! e
                }
            }
        }
    }

    error {
        case e: ObjectifyException => {
            status = e.status
            e.getMessage
        }
        case e: ObjectifyExceptionWithCause => {
            status = e.status
            e.getMessage
        }
    }
}