package org.objectify

/**
  * Syntacic sugar for simplifying Objectify definitions
  */
trait ObjectifySugar {
    def ~:[T <: AnyRef](implicit manifest: Manifest[T]): Class[T] = {
        manifest.erasure.asInstanceOf[Class[T]]
    }

    def -:[T <: AnyRef](implicit manifest: Manifest[T]): Option[Class[T]] = {
        Some(manifest.erasure.asInstanceOf[Class[T]])
    }
}
