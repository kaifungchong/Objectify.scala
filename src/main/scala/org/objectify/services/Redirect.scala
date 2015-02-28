package org.objectify.services

/**
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
case class Redirect(to: String, params: Option[Map[String, String]]) {

  def url = params.map(entry => {
    val (key, value) = entry
    s"$key=$value"
  }) match {
    case Some(queryString) => s"$to?${queryString.toList mkString "&"}"
    case None => to
  }

}
