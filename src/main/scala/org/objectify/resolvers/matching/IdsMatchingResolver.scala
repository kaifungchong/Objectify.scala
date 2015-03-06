package org.objectify.resolvers.matching

import mojolly.inflector.Inflector
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.resolvers.Resolver

/**
 * This is a match class resolver it's goal is to look for an ID that passes
 * the named param it's trying to resolve.
 *
 * In this case /^(.*)Id/
 *
 * It should follow the rubric of looking in the following places:
 *
 * 1. Payload Param {"ids": [:id1, :id2, :id3]} @Named("Ids") id:List[Int]  {"courseIds": :courseIds} @Named("CourseIds") id:List[Int]
 * 2. Query param ie /courses?ids=:id1,:id2,:id3 @Named("Ids") id:List[Int]  /concepts?courseIds=:courseId1,:courseId2,:courseId3 @Named("CourseIds") id:List[Int]
 *
 */
case class IdsMatchingResolver(named: String) extends MatchingResolver[List[Int]] {
  override def apply(param: ObjectifyRequestAdapter): List[Int] = {

    // Id => id, CourseId => courseId
    val camelizedNamed = Inflector.uncapitalize(named)
    val idsList = param.getQueryParameters.get(camelizedNamed) match {
      case Some(list) => list
      case None => {

        val IdJson = s""".*"$camelizedNamed"\s*:\s*\[(\s*\d+\s*,)*\].*""".r
        val IdXml = s"""<$camelizedNamed>(\d+)</$camelizedNamed>""".r

        val stringList = param.getBody match {
          case IdJson(ids) => ids
          case IdXml(ids) => ids
          // text
          case ids: String => ids
        }

        stringList.split(',').toList
      }
    }

    idsList.map(_.toInt)
  }
}
