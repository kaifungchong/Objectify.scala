package org.objectify.executor

import org.objectify.adapters.ObjectifyRequestAdapter

/**
  * This is a wrapper for a request
  */
class ObjectifyRequest(request: ObjectifyRequestAdapter) {
    def getPath = request.getPath

    def getQueryParameters = request.getQueryParameters

    def getHttpMethod = request.getHttpMethod
}
