package org.objectify.adapters

import org.objectify.HttpMethod.Delete
import org.objectify.HttpMethod.Get
import org.objectify.HttpMethod.Options
import org.objectify.HttpMethod.Patch
import org.objectify.HttpMethod.Post
import org.objectify.HttpMethod.Put
import org.objectify.Objectify
import org.scalatra.servlet.{RichResponse, RichRequest, ServletBase}
import org.objectify.exceptions.{ObjectifyExceptionWithCause, ObjectifyException, BadRequestException}
import org.scalatra.fileupload.FileUploadSupport

trait ObjectifyScalatraAdapter extends Objectify with ServletBase with FileUploadSupport {

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
        sortedActions.foreach(action => {
            val scalatraFunction = (action.method match {
                case Options => options _
                case Get => get _
                case Post => post _
                case Put => put _
                case Delete => delete _
                case Patch => patch _
            })

            scalatraFunction("/" + action.route.getOrElse(throw new BadRequestException("No Route Found"))) {
                // wrap HttpServletRequest in adapter and get ObjectifyResponse
                val objectifyResponse = execute(action,
                    new ScalatraRequestAdapter(RichRequest(request), RichResponse(response), params.toMap, Some(fileParams)))

                // find appropriate response adapter and serialize the response
                locateResponseAdapter(objectifyResponse).serializeResponseAny(request, response, objectifyResponse)
            }
        })
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