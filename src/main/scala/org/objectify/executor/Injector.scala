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
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.ConfigurationException
import org.objectify.resolvers.matching.{MatchingResolvers, MatchingResolver}
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
                                    genericTypesOption: Option[Seq[Class[_]]] = None,
                                    prefix: String = ""
                                    ): Any = {

    val returnType = classTag[P].runtimeClass.asInstanceOf[Class[P]]

    val (resolverName, paramName) = paramAnnotation match {
      case Some(annotation: Named) => (s"${annotation.value()}Resolver", Some(annotation.value()))
      case _ => (specifyName(paramType, genericTypesOption), None)
    }
    val resolverOption = ClassResolver.resolveResolverClass(resolverName, paramType, returnType)

    val resolvedValue = (resolverOption, paramName) match {

      // if we found a resolver lets kick it off
      case (Some(resolver), _) =>
        logger.trace(s"${prefix}Running Resolver: ${resolver.getSimpleName}")
        Option(Invoker.invoke(resolver, resolverParam, prefix = prefix + "\t").apply(resolverParam))

      // if now maybe we have a generic resolver
      case (None, Some(argumentName)) =>
        MatchingResolvers.resolverForName(argumentName) match {
          case Some(resolverClass) =>
            logger.trace(s"${prefix}Running Resolver: ${resolverClass.getSimpleName}")
            val resolver = resolverClass.getConstructor(classOf[String]).newInstance(argumentName).asInstanceOf[MatchingResolver[P]]
            Option(resolver(resolverParam.asInstanceOf[ObjectifyRequestAdapter]))

          case None => None
        }

      case _ =>
        val constructors = paramType.getConstructors

        // if the class has exactly one constructor with no arguments then we can resolve
        // it by simply instantiating it.
        if (constructors.length == 1 && constructors.head.getParameterTypes.length == 0) {
          Some(paramType.newInstance())
        }
        else {
          throw ConfigurationException(s"Unable to determine a resolver for this particular value. [${paramType.getSimpleName}]")
        }
    }

    //    logger.trace(s"$prefix  Yielding: $resolvedValue")
    resolvedValue.getOrElse(throw ConfigurationException(s"Unable to resolve value of type [${paramType.getSimpleName}]"))
  }

  private def specifyName(clazz: Class[_]) = {
    clazz.getSimpleName + "Resolver"
  }

  private def specifyName(rawType: Class[_], genTypes: Option[Seq[Class[_]]] = None) = {
    val sb = new StringBuilder
    sb append rawType.getSimpleName
    genTypes.foreach(_.foreach(sb append _.getSimpleName))
    sb append "Resolver"
    sb.mkString
  }
}

