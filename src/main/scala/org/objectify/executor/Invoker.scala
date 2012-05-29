package org.objectify.executor


/**
 * This class is responsible for invoking instances of classes.
 *
 * @author Arthur Gonigberg
 * @since 12-05-24
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
  def invoke[T, P: ClassManifest](clazz: Class[_ <: T], resolverParam: P): T = {
    /*
    1. find the constructor annotations
    2. for each constructor annotation, instantiate a resolver and pass it the above parameter
    3. take the above list of resolved values and pass it into the constructor to instantiate the clazz
    4. return instance
     */

    // assume only one constructor
    val constructor = clazz.getConstructors.head
    val injectedValues = Injector.getInjectedResolverParams(constructor, resolverParam)

    if (!injectedValues.isEmpty) {
      // convert list to var args
      constructor.newInstance(injectedValues.map{_.asInstanceOf[AnyRef]}:_*).asInstanceOf[T]
    }
    else {
      // assume there is no parameterized constructor
      clazz.newInstance()
    }
  }

  def invoke[T](klass: Class[_ <: T]): T = {
    klass.newInstance()
  }
}

