package org.objectify.adapters

/**
  * Response adapter for String
  */
class StringResponseAdapter extends ObjectifyResponseAdapter[String] {
    def serializeResponse(dto: String) = dto
}
