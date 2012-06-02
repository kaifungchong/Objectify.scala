package org.objectify.adapters

import org.objectify.HttpMethod.Delete
import org.objectify.HttpMethod.Get
import org.objectify.HttpMethod.Options
import org.objectify.HttpMethod.Patch
import org.objectify.HttpMethod.Post
import org.objectify.HttpMethod.Put
import org.objectify.Objectify
import org.scalatra.servlet.ServletBase
import org.objectify.exceptions.BadRequestException

trait ObjectifyScalatraAdapter extends Objectify with ServletBase {

    /**
      * Decorates the default bootstrap which has the configuration
      * validation in it
      */
    override def bootstrap() {
        super.bootstrap()

        /**
          * For each action we map to the Scalatra equivalent block parameter
          * though we are still bound to the HttpServletRequest and Response
          * currently.
          */
        actions.foreach(action => {
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
                val objectifyResponse = execute(action, new HttpServletRequestAdapter(request))

                // populate HttpServletResponse with ObjectifyResponse fields
                response.setContentType(objectifyResponse.contentType)
                response.setStatus(objectifyResponse.status)
                response.getWriter.print(objectifyResponse.getSerializedEntity)
            }
        })
    }
}