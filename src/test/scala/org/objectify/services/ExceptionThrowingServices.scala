package org.objectify.services

import org.objectify.exceptions.{ObjectifyException, ConfigurationException, BadRequestException}


/**
  * This simulates a broken service
  */
class ExceptionThrowingServices {}

class ThrowsBadRequest extends Service[String] {
    def apply():String = throw new BadRequestException("somethingawful")
}

class ThrowsUnexpected extends Service[String] {
    def apply():String = throw new NullPointerException("whoa whoa whoa... hold up man... whoa")
}

class ThrowsConfig extends Service[String] {
    def apply():String = throw new ConfigurationException("somethingawful")
}

class Throws403 extends Service[String] {
    def apply():String = throw new ObjectifyException(403, "somethingawful")
}

