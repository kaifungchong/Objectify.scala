/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import java.lang.reflect.{Constructor, ParameterizedType}
import javax.inject.Named

import com.twitter.logging.Logger
import org.objectify.annotations.ResolveWith
import org.objectify.resolvers.{ClassResolver, Resolver}

import scala.collection.mutable.ListBuffer
import scala.reflect.{ClassTag, classTag}

/**
 * This object is responsible for finding, instantiating and executing resolvers for the given constructor
 */
private[executor] object Injector {

  val logger = Logger("resolver")

  /**
   * Create a list of parameters that have been resolved, assuming they all require the given parameter
   *
   * @param constructor - the constructor to inject
   * @param resolverParam - the parameter value to resolve with
   * @return - the values to be passed to the injectable class
   */
  def getInjectedResolverParams[P: ClassTag](constructor: Constructor[_], resolverParam: P, prefix: String = ""): List[Any] = {
    val constructorValues = ListBuffer[Any]()

    (constructor.getGenericParameterTypes, constructor.getParameterAnnotations).zipped.foreach {
      (genParamType, paramAnnotations) =>
        // assume only one annotation per parameter
        val paramAnnotation = paramAnnotations.headOption

        genParamType match {

          // if it an instance of class then it has no generic parameter
          case paramType: Class[_] =>
            constructorValues += invokeParameter(paramAnnotation, paramType, resolverParam, prefix = prefix + "\t")

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

            constructorValues += invokeParameter(paramAnnotation, rawType, resolverParam, Some(genericTypes.toSeq), prefix + "\t")
          case _ => // nothing
        }
    }

    constructorValues.toList
  }

  def invokeParameter[P: ClassTag](
                                    paramAnnotation: Option[java.lang.annotation.Annotation],
                                    paramType: Class[_],
                                    resolverParam: P,
                                    genericTypes: Option[Seq[Class[_]]] = None,
                                    prefix: String = ""
                                    ): Any = {

    // if annotated, easy to find resolver
    val resolver = if (paramAnnotation.isDefined && (paramAnnotation.get.isInstanceOf[Named] || paramAnnotation.get.isInstanceOf[ResolveWith])) {
      val resolverName = paramAnnotation.get match {
        case namedAnnotation: Named => {
          namedAnnotation.value() + "Resolver"
        }
        case resolvedWith: ResolveWith => resolvedWith.value()
        case _ => "FailedResolver"
      }
      val namedResolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
        resolverName,
        paramType,
        classTag[P].runtimeClass.asInstanceOf[Class[P]]
      )

      namedResolver
    }
    // if not, try to load resolver based on type
    else {
      val className = if (genericTypes.isDefined) specifyName(paramType, genericTypes.get) else specifyName(paramType)

      val classResolver: Class[Resolver[_, P]] = ClassResolver.resolveResolverClass(
        className,
        paramType,
        classTag[P].runtimeClass.asInstanceOf[Class[P]]
      )

      classResolver
    }

    logger.debug(s"${prefix}Running Resolver: ${resolver.getSimpleName}")
    val resolvedValue = Invoker.invoke(resolver, resolverParam, prefix = prefix + "\t").apply(resolverParam)

    logger.debug(s"$prefix  Yielding: $resolvedValue")

    resolvedValue
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

