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
 od
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
    val camelizedNamed = Inflector.uncapitalize(named.replace("Option", ""))
    val idsListOption: Option[List[String]] = param.getQueryParameters.get(camelizedNamed) match {
      case Some(list) => Some(list)
      case None => {

        val IdJson = s""".*"$camelizedNamed"\\s*:\\s*\\[([^\\]]*)].*""".r
        val IdXml = s""".*<$camelizedNamed>(\\d+)</$camelizedNamed>.*""".r

        param.getBody match {
          case IdJson(ids) => Some(ids.split(",").toList)
          case IdXml(ids) => Some(ids.split(",").toList)
          case _ => None
        }
      }
    }

    try {
      idsListOption match {
        case Some(idsList) =>
          // remove empty entries and map to integerse
          Some(idsList.filter(_ != "").map(_.toInt))
        case None => None
      }
    }
    catch {
      case nfe: NumberFormatException =>
        throw BadRequestException(s"$named was not a list of integers as expected")
    }
  }
}
