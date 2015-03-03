/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import org.apache.commons.fileupload.FileItem
import org.objectify.adapters.ObjectifyRequestAdapter

/**
 * Resolve a file being uploaded
 */
class FileItemResolver extends Resolver[FileItem, ObjectifyRequestAdapter] {
  def apply(req: ObjectifyRequestAdapter) = {
    req.getFileParams.head._2
  }
}
