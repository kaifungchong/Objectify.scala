/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.responders

import org.objectify.exceptions.ConfigurationException
import org.objectify.adapters.{FormattedResponse, EntityResponse}


/**
  * The responder's job is to take a result from the service and format it so
  * that it can be returned to the web framework.
  */
abstract class ServiceResponder[T, P: Manifest] {
    var status:Option[Int] = None
    var contentType:Option[String] = None

    final def applyAny(serviceResult: Any): T = {
        if (serviceResult != null && serviceResult.isInstanceOf[P]) {
            // handle special case for casting booleans between Scala and Java
            serviceResult match {
                case result: java.lang.Boolean if castClass.equals(classOf[Boolean]) =>
                    apply(Boolean2boolean(result).asInstanceOf[P])
                case _ =>
                    apply(castClass.cast(serviceResult))
            }
        }
        else {
            throw new ConfigurationException("The service and responder provided are not compatible.")
        }
    }

    private def castClass: Class[P] = manifest[P].runtimeClass.asInstanceOf[Class[P]]

    // convenience function for wrapping entity responses
    implicit def entity2Formatted(er: EntityResponse[_]) = new FormattedResponse(er)

    def apply(serviceResult: P): T
}
