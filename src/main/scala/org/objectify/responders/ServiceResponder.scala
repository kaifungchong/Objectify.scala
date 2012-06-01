package org.objectify.responders


/**
  * The responder's job is to take a result from the service and format it so
  * that it can be returned to the web framework.
  */
abstract class ServiceResponder[T, P: Manifest] {

    final def applyAny(serviceResult: Any): T = {
        if (serviceResult != null && serviceResult.isInstanceOf[P]) {
            apply(castClass.cast(serviceResult))
        }
        else {
            throw new IllegalArgumentException("The service and responder provided are not compatible.")
        }
    }

    private def castClass: Class[P] = manifest[P].erasure.asInstanceOf[Class[P]]

    def apply(serviceResult: P): T
}