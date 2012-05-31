package org.objectify.adapters

import org.objectify.HttpMethod.Delete
import org.objectify.HttpMethod.Get
import org.objectify.HttpMethod.Options
import org.objectify.HttpMethod.Patch
import org.objectify.HttpMethod.Post
import org.objectify.HttpMethod.Put
import org.objectify.Objectify
import org.scalatra.servlet.ServletBase

trait ScalatraAdapter extends Objectify with ServletBase {

    /**
      * Decorates the default bootstrap which has the configuration
      * validation in it
      */
    override def bootstrap = {
        super.bootstrap

        /**
          * For each action we map to the scalatra equivalent block paramter
          * though we are still bound to the HttpServletRequest and Response
          * currently.
          */
        actions.foreach(action => {
            val scalatraFunction = (action.method match {
                case Options => options _
                case Get     => get _
                case Post    => post _
                case Put     => put _
                case Delete  => delete _
                case Patch   => patch _
            })
            
            scalatraFunction(action.route.getOrElse(throw new RuntimeException("No Route Found"))) {
                execute(action, request, response)
            }
        })
    }
}