package org.objectify.responders

import org.objectify.services.Service

trait Responder {
    def apply(service: Option[_ <: Service]): String
}