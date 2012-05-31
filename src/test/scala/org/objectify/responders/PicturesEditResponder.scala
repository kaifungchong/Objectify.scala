package org.objectify.responders


/**
  * Sample Responder class
  */
class PicturesEditResponder extends ServiceResponder[String, String] {
    override def apply(serviceResult: String): String = {
        serviceResult
    }
}