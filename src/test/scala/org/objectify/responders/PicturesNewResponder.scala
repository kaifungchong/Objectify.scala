package org.objectify.responders


/**
  * Sample Responder class
  */
class PicturesNewResponder extends ServiceResponder[String, String] {
override def apply(serviceResult: String): String = {
serviceResult
}
}
