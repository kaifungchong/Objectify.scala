package org.objectify

trait ObjectifySugar {
    def ~:[T <: AnyRef](implicit manifest: Manifest[T]): Class[T] = {
        manifest.erasure.asInstanceOf[Class[T]]
    }
}
