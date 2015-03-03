/*-------------------------------------------------------------------------------------------------
 - Project:   unicorn                                                                             -
 - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                         -
 - Property of Learndot. Not for public distribution. For your eyes only!                         -
 -------------------------------------------------------------------------------------------------*/

package org.objectify.services

object ResultStatus extends Enumeration {
  type ResponseStatus = Value

  val Ok = Value("Ok")
  val Failed = Value("Failed")
}


case class Result(message: Option[String] = None,
                  error: Option[String] = None) {
  lazy val status = error match {
    case Some(s) => ResultStatus.Failed
    case None => ResultStatus.Ok
  }
}

object Result {

  def failed(error: String): Result = {
    Result(error = Some(error))
  }

  def failed: Result = {
    failed("")
  }

  def ok: Result = {
    ok("")
  }

  def ok(message: String): Result = {
    Result(Some(message), None)
  }

}
