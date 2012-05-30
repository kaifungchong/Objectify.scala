package org.objectify

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions()) {
    override def toString = {
        "Objectify Configuration" + actions.toString
    }
}

trait ObjectifySugar {
    def ~:[T <: AnyRef](implicit manifest: Manifest[T]): Class[T] = {
        manifest.erasure.asInstanceOf[Class[T]]
    }
}
