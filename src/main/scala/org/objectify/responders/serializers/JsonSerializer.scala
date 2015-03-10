package org.objectify.responders.serializers

import com.codahale.jerkson.Json._

/**
 *
 * @author Joe Gaudet - (joe@learndot.com)
 */
trait JsonSerializer {
  def apply(any: Any): String
}


class JerksonJsonSerializers extends JsonSerializer {
  override def apply(any: Any): String = {
    generate(any)
  }
}
