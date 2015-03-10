package org.objectify

import mojolly.inflector.Inflector

import scala.util.Random

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
trait TestHelpers {


  val words = List("Course", "Word", "Test", "Foo", "Bar", "Baz")

  private val random = new Random

  def anonymousInt(n: Int) = random.nextInt(n)

  def anonymousInt = random.nextInt()

  def anonymousId = Math.abs(random.nextInt())

  def anonymousString(n: Int) = random.alphanumeric(n)

  def anonymousString = random.alphanumeric

  def anonymousInstanceName = Inflector.uncapitalize(anonymousClassName)

  def anonymousClassName = {
    (1 to (3 + anonymousInt(5))).map(_ => words(anonymousInt(words.length - 1))).mkString
  }

}

object Test extends TestHelpers {

  def main(args: Array[String]) {

    (1 to 100).foreach(_ => println(anonymousClassName))

  }

}
