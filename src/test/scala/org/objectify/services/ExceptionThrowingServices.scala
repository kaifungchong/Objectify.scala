package org.objectify.services

import org.objectify.exceptions.{ObjectifyException, ConfigurationException, BadRequestException}


/**
  * This simulates a broken service
  */
class ExceptionThrowingServices {}

class ThrowsBadRequest extends Service[String] {
    def apply() = throw new BadRequestException("somethingawful")
}

class ThrowsConfig extends Service[String] {
    def apply() = throw new ConfigurationException("somethingawful")
}

class Throws403 extends Service[String] {
    def apply() = throw new ObjectifyException(403, "somethingawful")
}

