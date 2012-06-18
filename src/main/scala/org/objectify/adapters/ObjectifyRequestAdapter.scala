package org.objectify.adapters

import org.objectify.HttpMethod
import org.apache.commons.fileupload.FileItem

/**
  * Adapter for requests
  */
trait ObjectifyRequestAdapter {
    def getPath: String

    def getQueryParameters: Map[String, List[String]]

    def getPathParameters: Map[String, String]

    def getHttpMethod: HttpMethod.Value

    def getBody: String

    def getFileParams: Map[String, FileItem]
}
