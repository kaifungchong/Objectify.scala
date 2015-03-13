package org.objectify.services

/**
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
case class RedirectResult(to: String, params: Map[String, String] = Map()) {

  def url = {
    val queryString = params.map(entry => {
      val (key, value) = entry
      s"$key=$value"
    }).mkString("&")
    if (queryString.isEmpty) to else s"$to?$queryString"
  }
}