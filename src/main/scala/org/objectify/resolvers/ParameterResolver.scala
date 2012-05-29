package org.objectify.resolvers


/**
 * This class will determine which resolvers are needed for fulfilling the parameters
 *
 * @author Arthur Gonigberg
 * @since 12-05-24
 */
object ParameterResolver {
  private val PREFIX = "call"
  private val SUFFIX = "_$eq"

  def populateParametersWithResolver[T: ClassManifest, P: ClassManifest](instance: T, param: P) {

    val injectables = instance.getClass.getMethods.filter(method => method.getName.startsWith(PREFIX) && method.getName.endsWith(SUFFIX))

    for {injectable <- injectables} {
      val resolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
        specifyClassName(injectable.getName),
        injectable.getParameterTypes.head,
        classManifest[P].erasure.asInstanceOf[Class[P]]
      )
      val resolverInstance: Resolver[_, P] = resolver.newInstance()
      injectable.invoke(instance, resolverInstance(param).asInstanceOf[AnyRef])
    }
  }

  def specifyClassName(fieldName: String) = {
    val removedPrefix = fieldName.substring(PREFIX.length)
    val removedBoth = removedPrefix.substring(0, removedPrefix.length - SUFFIX.length)

    removedBoth + "Resolver"
  }
}
