package org.objectify

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions()) {
    override def toString = {
        "Objectify Configuration" + actions.toString
    }
    
    def bootstrap = {
    	actions.bootstrapValidation
    }
    
    def execute(action:Action, request: HttpServletRequest, response: HttpServletResponse) = {
        
    }
}

trait ObjectifySugar {
    def ~:[T <: AnyRef](implicit manifest: Manifest[T]): Class[T] = {
        manifest.erasure.asInstanceOf[Class[T]]
    }
}
