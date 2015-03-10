/*-------------------------------------------------------------------------------------------------
 - Project:   unicorn                                                                             -
 - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                         -
 - Property of Learndot. Not for public distribution. For your eyes only!                         -
 -------------------------------------------------------------------------------------------------*/

package org.objectify.services

case class ResultWithValue(value: Any,
                           message: Option[String] = None,
                           error: Option[String] = None) {
  lazy val status = error match {
    case Some(s) => ResultStatus.Failed
    case None => ResultStatus.Ok
  }
}

object ResultWithValue {

  def failed(error: String, value: Any): ResultWithValue = {
    new ResultWithValue(value, error = Some(error))
  }

  def failed(error: String, value: (Any, Any)*): ResultWithValue = {
    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), None, Some(error))
  }

  def ok(value: (Any, Any)*): ResultWithValue = {
    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), None, None)
  }

  def ok(value: Any): ResultWithValue = {
    new ResultWithValue(value, None, None)
  }

  def ok(value: Map[Any, Any]): ResultWithValue = {
    new ResultWithValue(value, None, None)
  }

  def ok(message: String, value: (Any, Any)*): ResultWithValue = {
    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), Some(message), None)
  }

  def ok(message: String, value: Map[Any, Any]): ResultWithValue = {
    ok(message, value)
  }

  def ok(message: String, value: Any): ResultWithValue = {
    new ResultWithValue(value, Some(message), None)
  }

}
