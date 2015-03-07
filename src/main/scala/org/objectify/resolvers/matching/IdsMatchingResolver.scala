package org.objectify.resolvers.matching

import mojolly.inflector.Inflector
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException
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

  val idsOptionResolver = IdsOptionMatchingResolver(named)

  override def apply(param: ObjectifyRequestAdapter): List[Int] = {
    idsOptionResolver(param).getOrElse(throw BadRequestException(s"Expected to be able to extract $named from the queryParam, or request body."))
  }
}

case class IdsOptionMatchingResolver(named: String) extends MatchingResolver[Option[List[Int]]] {
  override def apply(param: ObjectifyRequestAdapter): Option[List[Int]] = {
    // Id => id, CourseId => courseId
    val camelizedNamed = Inflector.uncapitalize(named)
    val idsList = param.getQueryParameters.get(camelizedNamed) match {
      case Some(list) => list
      case None => {

        println(param.getBody)

        val IdJson = s""".*"$camelizedNamed"\\s*:\\s*\\[([^\\]]*)].*""".r
        val IdXml = s""".*<$camelizedNamed>(\\d+)</$camelizedNamed>.*""".r

        val stringList = param.getBody match {
          case IdJson(ids) => ids
          case IdXml(ids) => ids
          // text
          case ids: String => ids
        }

        stringList.split(',').toList
      }
    }

    try {
      Option(idsList.map(_.toInt))
    }
    catch {
      case nfe: NumberFormatException => throw BadRequestException(s"$named was not a list of integers as expected")
    }
  }
}
