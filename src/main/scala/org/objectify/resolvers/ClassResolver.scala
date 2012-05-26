package org.objectify.resolvers

import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.Reflections
import scala.collection.JavaConversions._
import org.objectify.policies.Policy
import org.objectify.services.Service
import org.objectify.responders.Responder


object ClassResolver {
  private val classLoader = this.getClass.getClassLoader
  private val reflections = new Reflections(new ConfigurationBuilder().setScanners(new ResourcesScanner()))

  private val policies = subClassesOf(classOf[Policy])
  private val services = subClassesOf(classOf[Service])
  private val responders = subClassesOf(classOf[Responder])

  def resolvePolicyClass(string: String) = {
    resolveClass(string, policies)
  }

  def resolveServiceClass(string: String) = {
    resolveClass(string, services)
  }

  def resolveResponderClass(string: String) = {
    resolveClass(string, responders)
  }

  private def resolveClass[T](className: String, set: Set[Class[T]]): Class[T] = {
    set.find(_.getName.matches(className)).getOrElse(throw new ClassNotFoundException("No class matching the name:" + className))
  }

  private def subClassesOf[T](klass: Class[T]): Set[Class[T]] = {
    val set = reflections.getStore().getSubTypesOf(klass.toString).toSet
    val mapped = set.map(className => classLoader.loadClass(className))
    mapped.asInstanceOf[Set[Class[T]]]
  }
}