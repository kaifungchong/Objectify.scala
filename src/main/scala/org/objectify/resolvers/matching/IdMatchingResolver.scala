package org.objectify.resolvers.matching

import java.io.Serializable

import mojolly.inflector.Inflector
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException

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

  val idOptionResolver = IdOptionMatchingResolver(named)

  override def apply(param: ObjectifyRequestAdapter): Int = {
    idOptionResolver(param).getOrElse(throw BadRequestException(s"Expected to be able to extract $named from the path, query, or request body."))
  }
}

case class IdOptionMatchingResolver(named: String) extends MatchingResolver[Option[Int]] {
  override def apply(param: ObjectifyRequestAdapter): Option[Int] = {

    // Id => id, CourseId => courseId
    val camelizedNamed = Inflector.uncapitalize(named)

    val pathId = param.getPathParameters.get(camelizedNamed)
    val queryParam = param.getQueryParameters.get(camelizedNamed)

    val stringOption: Option[String] = (pathId, queryParam) match {
      case (Some(id), Some(listId)) => Some(id)
      case (Some(id), None) => Some(id)
      case (None, Some(listId)) => listId.headOption
      case (None, None) => {
        val IdJson = s""".*"$camelizedNamed":\\s*(\\d+).*""".r
        val IdXml = s""".*<$camelizedNamed>(\\d+)</$camelizedNamed>.*""".r

        param.getBody match {
          case IdJson(id) => Some(id)
          case IdXml(id) => Some(id)
          case _ => None
        }
      }
    }

    stringOption.map(_.toInt)
  }
}
