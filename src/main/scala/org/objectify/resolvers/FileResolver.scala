package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter
import org.apache.commons.fileupload.FileItem

/**
  * Resolve a file being uploaded
  */
class FileResolver extends Resolver[FileItem, ObjectifyRequestAdapter] {
    def apply(req: ObjectifyRequestAdapter) = {
        req.getFileParams.head._2
    }
}
