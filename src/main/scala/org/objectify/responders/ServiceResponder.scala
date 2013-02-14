package org.objectify.responders

import org.objectify.exceptions.ConfigurationException


/**
  * The responder's job is to take a result from the service and format it so
  * that it can be returned to the web framework.
  */
abstract class ServiceResponder[T, P: Manifest] {
    var status:Option[Int] = None
    var contentType:Option[String] = None

    final def applyAny(serviceResult: Any): T = {
        if (serviceResult != null && serviceResult.isInstanceOf[P]) {
            if (serviceResult.isInstanceOf[java.lang.Boolean] &&
                castClass.equals(classOf[Boolean])) {
                apply(Boolean2boolean(serviceResult.asInstanceOf[java.lang.Boolean]).asInstanceOf[P])
            }
            else {
                apply(castClass.cast(serviceResult))
            }
        }
        else {
            throw new ConfigurationException("The service and responder provided are not compatible.")
        }
    }

    private def castClass: Class[P] = manifest[P].erasure.asInstanceOf[Class[P]]

    def apply(serviceResult: P): T
}
