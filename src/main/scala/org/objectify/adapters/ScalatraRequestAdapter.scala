package org.objectify.adapters

import org.scalatra.Request
import org.objectify.HttpMethod
import org.objectify.exceptions.BadRequestException
import org.apache.commons.fileupload.FileItem

/**
  * Scalatrafied Request!
  */
class ScalatraRequestAdapter(request: Request, pathParameters: Map[String, String],
                             fileParams: Option[collection.Map[String, FileItem]] = None)
    extends ObjectifyRequestAdapter {

    def getPath = request.pathInfo

    def getQueryParameters = request.multiParameters.map(entry => (entry._1, entry._2.toList))

    def getPathParameters = pathParameters

    def getHttpMethod = HttpMethod.values.find(_.toString.equalsIgnoreCase(request.requestMethod.toString))
        .getOrElse(throw new BadRequestException("Could not parse HTTP method."))

    def getBody = request.body

    def getFileParams = {
        val fp = fileParams.getOrElse(Map())
        fp.toMap[String,FileItem]
    }
}
