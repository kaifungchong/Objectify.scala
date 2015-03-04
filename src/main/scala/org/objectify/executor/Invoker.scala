/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import scala.reflect.ClassTag

/**
 * This class is responsible for invoking instances of classes.
 */
private[executor] object Invoker {

  /**
   * This method will do constructor injection on resolvers, assuming they all take
   * the same parameter -- the resolverParam passed in.
   *
   * @param clazz - the class to instantiate
   * @param resolverParam - the resolver parameter to inject the constructor of the resolver with
   * @return - a dependency-injected instance
   */
  def invoke[T, P: ClassTag](clazz: Class[_ <: T], resolverParam: P, prefix: String = ""): T = {

    /*
1. find the constructor annotations
2. for each constructor annotation, instantiate a resolver and pass it the above parameter
3. take the above list of resolved values and pass it into the constructor to instantiate the clazz
4. return instance
 */

    //    val tCons = clazz.type.typeConstructor
    //
    //    val params = tCons.members.filter(symbol => symbol.isParameter)
    //    println(params)

    // assume only one constructor
    val constructor = clazz.getConstructors.head


    val injectedValues = Injector.getInjectedResolverParams(constructor, resolverParam, prefix)

    val ret = if (injectedValues.nonEmpty) {
      // convert list to var args

      val ret = constructor.newInstance(injectedValues.map {
        _.asInstanceOf[AnyRef]
      }: _*).asInstanceOf[T]

      ret
    }
    else {
      // assume there is no parameterized constructor
      clazz.newInstance()
    }


    ret
  }

  def invoke[T](klass: Class[_ <: T]): T = {
    klass.newInstance()
  }
}
