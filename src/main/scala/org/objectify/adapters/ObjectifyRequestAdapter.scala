package org.objectify.adapters

import org.objectify.HttpMethod

/**
  * Adapter for requests
  */
trait ObjectifyRequestAdapter {
    def getPath: String

    def getQueryParameters: Map[String, Array[String]]

    def getHttpMethod: HttpMethod.Value
}
