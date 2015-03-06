/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

import java.io.FileNotFoundException
import java.lang.reflect.ParameterizedType
import java.net.URL
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.twitter.logging.Logger
import org.objectify.adapters.ObjectifyResponseAdapter
import org.objectify.exceptions.ConfigurationException
import org.objectify.executor.ObjectifyResponse
import org.objectify.policies.Policy
import org.objectify.responders.ServiceResponder
import org.objectify.services.Service
import org.reflections.Reflections
import org.reflections.scanners.{ResourcesScanner, SubTypesScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}
import org.streum.configrity._
import org.streum.configrity.yaml.YAMLFormat

import scala.collection.JavaConversions._

/**
 * This class is responsible for loading all the pertinent classes that need to be resolved or injected,
 * and caching them.
 */
object ClassResolver {
  private val logger = Logger(ClassResolver.getClass)
  private val reflections = new Reflections(new ConfigurationBuilder().setUrls(getScannableUrls)
    .setScanners(new ResourcesScanner(), new SubTypesScanner()))

  private val policies = subClassesOf(classOf[Policy])
  private val services = subClassesOf(classOf[Service[_]])
  private val responders = subClassesOf(classOf[ServiceResponder[_, _]])
  private val resolvers = subClassesOf(classOf[Resolver[_, _]])
  private val responseAdapters = subClassesOf(classOf[ObjectifyResponseAdapter[_]])

  def resolvePolicyClass(string: String) = {
    resolveClass(string, policies)
  }

  def resolveServiceClass(string: String) = {
    resolveClass(string, services)
  }

  private def resolveClass[T](className: String, set: Set[Class[T]]): Class[T] = {
    resolveClassOption(className, set).getOrElse(throw new ConfigurationException("No class matching the name: " + className))
  }

  private def resolveClassOption[T](className: String, set: Set[Class[T]]): Option[Class[T]] = {
    set.find(_.getSimpleName.matches(className))
  }

  def resolveResponderClass(string: String) = {
    resolveClass(string, responders)
  }

  def resolveResponderClassOption(string: String) = {
    resolveClassOption(string, responders)
  }

  def locateResponseAdapter[T](response: ObjectifyResponse[T]): ObjectifyResponseAdapter[T] = {
    resolveResponseAdapter(response.entity.getClass).newInstance().asInstanceOf[ObjectifyResponseAdapter[T]]
  }

  def resolveResponseAdapter[T](genericType: Class[T]): Class[ObjectifyResponseAdapter[T]] = {
    val paramTypes = Seq(classOf[HttpServletRequest], classOf[HttpServletResponse], classOf[ObjectifyResponse[T]])
    resolveClassByTypes("serializeResponse", classOf[Unit], paramTypes, genericType, responseAdapters)
      .asInstanceOf[Class[ObjectifyResponseAdapter[T]]]
  }

  private def resolveClassByTypes[T, R, P](methodName: String, returnType: Class[R], paramTypes: Seq[Class[_]],
                                           genericType: Class[_], set: Set[Class[T]]): Class[T] = {
    set.find(target => {
      val method = target.getMethod(methodName, paramTypes: _*)

      // make sure return types match
      method.getReturnType.equals(returnType) &&
        method.getGenericParameterTypes.exists(p => {
          // make sure generic parameter of ObjectifyResponse matches
          p.isInstanceOf[ParameterizedType] &&
            p.asInstanceOf[ParameterizedType].getActualTypeArguments.exists(_.equals(genericType))
        })
    }).getOrElse(
        throw new ConfigurationException(s"No class matching method [$methodName] param type [$paramTypes] return type [$returnType]"))
  }

  def resolveResolverClass[T, P](name: String, returnType: Class[T], paramType: Class[P]): Option[Class[Resolver[_, P]]] = {
    resolveClassWithReturn(name, "apply", returnType, paramType, resolvers).asInstanceOf[Option[Class[Resolver[_, P]]]]
  }

  private def resolveClassWithReturn[T, R, P](className: String, methodName: String, returnType: Class[R],
                                              paramType: Class[P], set: Set[Class[T]]): Option[Class[T]] = {
    set.find(target => target.getSimpleName.matches(className) && target.getMethod(methodName, paramType).getReturnType.equals(returnType))
  }


  //    .getOrElse(throw new ConfigurationException(s"No class matching name [$className] method [$methodName] param type [$paramType] return type [$returnType]"))

  private def subClassesOf[T](klass: Class[T]): Set[Class[T]] = {
    val set = reflections.getSubTypesOf(klass).toSet

    logger.info(klass.getSimpleName + " Mappings: ")
    set.toList.sortBy(_.getSimpleName).foreach(x => logger.info(x.getSimpleName))
    logger.info("")

    set.asInstanceOf[Set[Class[T]]]
  }


  private def getScannableUrls = {
    /**
     * Load objectify base packages if they exist in the objectify.yml
     * -- if they don't exist, search the entire classpath
     */
    val config = try {
      Configuration.loadResource("/objectify.yml", YAMLFormat)
    }
    catch {
      case e: FileNotFoundException => {
        logger.warning("Could not find objectify.yml, resorting to scanning entire classpath")
        null
      }
    }
    val basePackages = if (config != null) config[List[String]]("packages_to_scan") else Nil
    val urls: java.util.Collection[URL] = {
      if (basePackages.isEmpty) {
        logger.info("Scanning entire classpath for Objectify classes...")
        ClasspathHelper.forJavaClassPath()
      }
      else {
        val bp = "org.objectify" :: basePackages
        logger.info("Scanning base package specified in objectify.yml for Objectify classes: " + bp)
        bp.flatMap(pkg => ClasspathHelper.forPackage(pkg))
      }
    }

    urls
  }
}