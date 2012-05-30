package org.objectify.executor

import java.lang.reflect.Constructor
import collection.mutable.ListBuffer
import javax.inject.Named
import org.objectify.resolvers.{ ClassResolver, Resolver }

/**
  * This object is responsible for finding, instantiating and executing resolvers for the given constructor
  *
  * @author Arthur Gonigberg
  * @since 12-05-29
  */
private[executor] object Injector {

    /**
      * Create a list of parameters that have been resolved, assuming they all require the given parameter
      *
      * @param constructor - the constructor to inject
      * @param resolverParam - the parameter value to resolve with
      * @return - the values to be passed to the injectable class
      */
    def getInjectedResolverParams[P: ClassManifest](constructor: Constructor[_], resolverParam: P): List[Any] = {
        val constructorValues = ListBuffer[Any]()

        (constructor.getParameterTypes, constructor.getParameterAnnotations).zipped.foreach {
            (paramType, paramAnnotations) =>
                // assume only one annotation per parameter
                val paramAnnotation = paramAnnotations.headOption
                // if annotated, easy to find resolver
                if (paramAnnotation.isDefined && paramAnnotation.get.isInstanceOf[Named]) {
                    val namedAnno = paramAnnotation.get.asInstanceOf[Named]
                    val resolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
                        namedAnno.value(),
                        paramType,
                        classManifest[P].erasure.asInstanceOf[Class[P]]
                    )
                    constructorValues += resolver.newInstance()(resolverParam)
                }
                // if not, try to load resolver based on type
                else {
                    val resolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
                        specifyName(paramType),
                        paramType,
                        classManifest[P].erasure.asInstanceOf[Class[P]]
                    )
                    constructorValues += resolver.newInstance()(resolverParam)
                }
        }
        constructorValues.toList
    }

    private def specifyName(clazz: Class[_]) = {
        clazz.getSimpleName + "Resolver"
    }
}

