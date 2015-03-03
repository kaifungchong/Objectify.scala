/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException

/**
 * Resolve the index from the path
 */
class IdResolver extends Resolver[Int, ObjectifyRequestAdapter] {
  def apply(req: ObjectifyRequestAdapter) = {
    req.getPathParameters.get("id").getOrElse(throw new BadRequestException("Could not parse index from path.")).toInt
  }
}

/**
 * This is a match class resolver it's goal is to look for an ID that passes
 * the named param it's trying to resolve.
 *
 * In this case /^(.*)Id/
 *
 * It should follow the rubbric of looking in the following places:
 *
 * 1. Path param ie /courses/:id  @Named("Id") id:Int  /courses/:courseId @Named("CourseId") id:Int
 * 2. Query param ie /courses?id=:id @Named("Id") id:Int  /concepts?courseId=:courseId @Named("CourseId") id:Int
 * 3. Payload Param {"id": :id} @Named("Id") id:Int  {"courseId": :courseId} @Named("CourseId") id:Int
 *
 */
//class IdMatchingResolver(named: String) extends Resolver[Int, ObjectifyRequestAdapter] {
//  override def apply(param: ObjectifyRequestAdapter): Int = {
//
//    // Id => id, CourseId => courseId
//    val _named = Inflector.uncapitalize(named)
//
//    val idString: String = param.getPathParameters.getOrElse[String](named,
//      param.getQueryParameters.getOrElse[String](named, {
//
//        val IdJsonReg = s"""$_named:\W*(\d+)[^,]*""".r
//        try {
//          val IdJsonReg(id) = param.getBody
//
//          id
//        }
//        catch {
//          // failed Json lets try XML
//          case e: MatchError => {
//            val IdXmlReg = s"""<$_named>(\d+)</$_named>""".r
//            val IdXmlReg(id) = param.getBody
//
//            id
//          }
//        }
//      }))
//
//    idString.toInt
//  }
//}

///**
// * This is a match class resolver it's goal is to look for an ID that passes
// * the named param it's trying to resolve.
// *
// * In this case /^(.*)Id/
// *
// * It should follow the rubric of looking in the following places:
// *
// * 1. Payload Param {"ids": [:id1, :id2, :id3]} @Named("Ids") id:List[Int]  {"courseIds": :courseIds} @Named("CourseIds") id:List[Int]
// * 2. Query param ie /courses?ids=:id1,:id2,:id3 @Named("Ids") id:List[Int]  /concepts?courseIds=:courseId1,:courseId2,:courseId3 @Named("CourseIds") id:List[Int]
// *
// */
//class IdsMatchingResolver(named: String) extends Resolver[List[Int], ObjectifyRequestAdapter] {
//  override def apply(param: ObjectifyRequestAdapter): Int = {
//
//  }
//}
