package org.objectify.adapters

import org.objectify.HttpMethod

/**
  * Adapter for requests
  */
trait ObjectifyRequestAdapter {
    def getPath: String

    def getQueryParameters: Map[String, List[String]]

    def getPathParameters: Map[String, String]

    def getHttpMethod: HttpMethod.Value

    def getBody: String
}
