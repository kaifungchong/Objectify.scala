/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import java.lang.reflect.{ParameterizedType, Constructor}
import collection.mutable.ListBuffer
import javax.inject.Named
import org.objectify.resolvers.{ClassResolver, Resolver}
import scala.reflect.{ClassTag, classTag}

/**
  * This object is responsible for finding, instantiating and executing resolvers for the given constructor
  */
private[executor] object Injector {

    /**
      * Create a list of parameters that have been resolved, assuming they all require the given parameter
      *
      * @param constructor - the constructor to inject
      * @param resolverParam - the parameter value to resolve with
      * @return - the values to be passed to the injectable class
      */
    def getInjectedResolverParams[P: ClassTag](constructor: Constructor[_], resolverParam: P): List[Any] = {
        val constructorValues = ListBuffer[Any]()

        (constructor.getGenericParameterTypes, constructor.getParameterAnnotations).zipped.foreach {
            (genParamType, paramAnnotations) =>
                // assume only one annotation per parameter
                val paramAnnotation = paramAnnotations.headOption

                // if it an instance of class then it has no generic parameter
                genParamType match {
                    case paramType: Class[_] =>
                        constructorValues += invokeParameter(paramAnnotation, paramType, resolverParam)

                    case paramType: ParameterizedType =>
                        val rawType = paramType.getRawType.asInstanceOf[Class[_]]
                        val genericTypes = new ListBuffer[Class[_]]()

                        var pt = paramType
                        // iterate through all type parameters
                        while (pt.getActualTypeArguments.head.isInstanceOf[ParameterizedType]) {
                            pt = pt.getActualTypeArguments.head.asInstanceOf[ParameterizedType]
                            genericTypes += pt.getRawType.asInstanceOf[Class[_]]
                        }
                        // lastly add non-typed param
                        genericTypes += pt.getActualTypeArguments.head.asInstanceOf[Class[_]]

                        constructorValues += invokeParameter(paramAnnotation, rawType, resolverParam, Some(genericTypes.toSeq))
                    case _ => // nothing
                }
        }
        constructorValues.toList
    }

    def invokeParameter[P: ClassTag](paramAnnotation: Option[java.lang.annotation.Annotation], paramType: Class[_],
                                          resolverParam: P, genericTypes: Option[Seq[Class[_]]] = None): Any = {
        // if annotated, easy to find resolver
        if (paramAnnotation.isDefined && paramAnnotation.get.isInstanceOf[Named]) {
            val namedAnno = paramAnnotation.get.asInstanceOf[Named]
            val resolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
                namedAnno.value(),
                paramType,
                classTag[P].runtimeClass.asInstanceOf[Class[P]]
            )
            Invoker.invoke(resolver, resolverParam).apply(resolverParam)
        }
        // if not, try to load resolver based on type
        else {
            val className = if (genericTypes.isDefined) specifyName(paramType, genericTypes.get) else specifyName(paramType)

            val resolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
                className,
                paramType,
                classTag[P].runtimeClass.asInstanceOf[Class[P]]
            )
            Invoker.invoke(resolver, resolverParam).apply(resolverParam)
        }
    }

    private def specifyName(clazz: Class[_]) = {
        clazz.getSimpleName + "Resolver"
    }

    private def specifyName(rawType: Class[_], genTypes: Seq[Class[_]]) = {
        val sb = new StringBuilder
        sb append rawType.getSimpleName
        genTypes.foreach(sb append _.getSimpleName)
        sb append "Resolver"

        sb.mkString
    }
}

