package org.objectify.resolvers.matching

import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.resolvers.Resolver

import scala.util.matching.Regex

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
abstract class MatchingResolver[A] extends Resolver[A, ObjectifyRequestAdapter]

/**
 * A registry for resolves that match against a NamedProperty
 */
object MatchingResolvers {

  private var resolverClasses: Map[Regex, Any] = Map.empty

  resolver( """\w*IdOption""".r, classOf[IdOptionMatchingResolver])
  resolver( """\w*Id""".r, classOf[IdMatchingResolver])
  resolver( """\w*Ids""".r, classOf[IdsMatchingResolver])
  resolver( """\w*IdsOption""".r, classOf[IdsOptionMatchingResolver])

  def resolverForName[P, T <: MatchingResolver[P]](name: String): Option[Class[T]] = {
    val resolverKlass = resolverClasses.find({
      case (regex, resolverClass) => {
        name.matches(regex.regex)
      }
    })

    resolverKlass.map({
      case (regex, value) => {
        value.asInstanceOf[Class[T]]
      }
    })
  }

  def resolver[P <: MatchingResolver[_]](key: Regex, value: Class[P]) = {
    resolverClasses = resolverClasses + (key -> value)

    value
  }

  def resolver[T](key: Regex) = {
    resolverClasses(key) match {
      case Some(resolverClass) => Some(resolverClass.asInstanceOf[T])
      case None => None
    }
  }


}

