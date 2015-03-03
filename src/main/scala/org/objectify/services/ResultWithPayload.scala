/*-------------------------------------------------------------------------------------------------
 - Project:   unicorn                                                                             -
 - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                         -
 - Property of Learndot. Not for public distribution. For your eyes only!                         -
 -------------------------------------------------------------------------------------------------*/

package org.objectify.services

class ResultWithValue(value: Any,
                      message: Option[String] = None,
                      error: Option[String] = None
                       ) extends Result(message, error) {
}

object ResultWithValue {

  def failed(error: String, value: Any): ResultWithValue = {
    new ResultWithValue(value, error = Some(error))
  }

  def failed(value: Any): ResultWithValue = {
    failed(value)
  }

  def ok(value: Any): ResultWithValue = {
    ok(value, "")
  }

  def ok(value: Map[Any, Any]): ResultWithValue = {
    ok(value, "")
  }

  def ok(value: Map[Any, Any], message: String): ResultWithValue = {
    ok(value, message)
  }

  def ok(value: Any, message: String): ResultWithValue = {
    new ResultWithValue(value, Some(message), None)
  }

}
