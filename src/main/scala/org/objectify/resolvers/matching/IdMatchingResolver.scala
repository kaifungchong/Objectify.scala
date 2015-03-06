package org.objectify.resolvers.matching

import mojolly.inflector.Inflector
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException

import scala.util.matching.Regex

/**
 * This is a match class resolver it's goal is to look for an ID that passes
 * the named param it's trying to resolve.
 *
 *
 * It should follow the rubbric of looking in the following places:
 *
 * 1. Path param ie /courses/:id  @Named("Id") id:Int  /courses/:courseId @Named("CourseId") id:Int
 * 2. Query param ie /courses?id=:id @Named("Id") id:Int  /concepts?courseId=:courseId @Named("CourseId") id:Int
 * 3. Payload Param {"id": :id} @Named("Id") id:Int  {"courseId": :courseId} @Named("CourseId") id:Int
 *
 */
case class IdMatchingResolver(named: String) extends MatchingResolver[Int] {

  override def apply(param: ObjectifyRequestAdapter): Int = {

    // Id => id, CourseId => courseId
    val camelizedNamed = Inflector.uncapitalize(named)
    val idString = param.getPathParameters.getOrElse(camelizedNamed, {
      param.getQueryParameters.get(camelizedNamed) match {
        case Some(list) => list.headOption.getOrElse(throw BadRequestException(s"Query parameter $named was present but had no value"))
        case None => {
          val IdJson = s""".*"$camelizedNamed":\\s*(\\d+).*""".r
          val IdXml = s""".*<$camelizedNamed>(\\d+)</$camelizedNamed>.*""".r

          param.getBody match {
            case IdJson(id) => id
            case IdXml(id) => id
            case _ => throw BadRequestException(s"Expected to be able to extract $named:Int from the path, query, or body")
          }
        }
      }
    })

    idString.toInt
  }
}
