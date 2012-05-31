package org.objectify.executor

import org.objectify.resolvers.ClassResolver
import org.objectify.adapters.ObjectifyResponseAdapter

/**
  * This is a wrapper for a response
  */
class ObjectifyResponse[T: ClassManifest](val contentType: String, val status: Int, val entity: T) {
    def getSerializedEntity: String = {
        val responseAdapter = ClassResolver.resolveResponseAdapter(classManifest[T].erasure)
            .newInstance().asInstanceOf[ObjectifyResponseAdapter[T]]
        responseAdapter.serializeResponse(entity)
    }
}


