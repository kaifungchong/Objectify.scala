package org.objectify.executor
import collection.mutable.ListBuffer

/**
 * This class is responsible for invoking instances of classes.
 *
 * @author Arthur Gonigberg
 * @since 12-05-24
 */
private[executor] case class Invoker[T]() {
  def invoke(klass: Class[_ <: T]): T = {
    invoke(List(klass)).head
  }

  def invoke(classes: List[Class[_ <: T]]): List[T] = {
    val instances = ListBuffer[T]()
    classes.foreach(klass => {
      instances += klass.newInstance()
    })
    instances.toList
  }
}
