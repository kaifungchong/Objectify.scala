package org.objectify.resolvers

import org.reflections.Reflections
import scala.collection.JavaConversions._
import org.objectify.policies.Policy
import org.objectify.services.Service
import org.objectify.responders.ServiceResponder
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}
import org.objectify.adapters.ObjectifyResponseAdapter
import org.objectify.exceptions.ConfigurationException
import org.reflections.scanners.{SubTypesScanner, ResourcesScanner}

/**
  * This class is responsible for loading all the pertinent classes that need to be resolved or injected,
  * and caching them.
  */
object ClassResolver {
    private val reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forJavaClassPath())
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

    def resolveResponderClass(string: String) = {
        resolveClass(string, responders)
    }

    def resolveResolverClass[T, P](name: String, returnType: Class[T], paramType: Class[P]): Class[Resolver[_, P]] = {
        resolveClassWithReturn(name, "apply", returnType, paramType, resolvers).asInstanceOf[Class[Resolver[_, P]]]
    }

    def resolveResponseAdapter[T](paramType: Class[T]): Class[ObjectifyResponseAdapter[T]] = {
        resolveClassByTypes("serializeResponse", classOf[String], paramType, responseAdapters).asInstanceOf[Class[ObjectifyResponseAdapter[T]]]
    }

    private def resolveClassByTypes[T, R, P](methodName: String, returnType: Class[R], paramType: Class[P], set: Set[Class[T]]): Class[T] = {
        set.find(target => target.getMethod(methodName, paramType).getReturnType.equals(returnType))
            .getOrElse(throw new ConfigurationException("No class matching method [%s] param type [%s] return type [%s]".format(methodName, paramType, returnType)))
    }

    private def resolveClass[T](className: String, set: Set[Class[T]]): Class[T] = {
        set.find(_.getSimpleName.matches(className)).getOrElse(throw new ConfigurationException("No class matching the name: " + className))
    }

    private def resolveClassWithReturn[T, R, P](className: String, methodName: String, returnType: Class[R], paramType: Class[P], set: Set[Class[T]]): Class[T] = {
        set.find(target => target.getSimpleName.matches(className) && target.getMethod(methodName, paramType).getReturnType.equals(returnType))
            .getOrElse(throw new ConfigurationException("No class matching method [%s] param type [%s] return type [%s]".format(methodName, paramType, returnType)))
    }

    private def subClassesOf[T](klass: Class[T]): Set[Class[T]] = {
        val set = reflections.getSubTypesOf(klass).toSet

        println(klass.getSimpleName + " Mappings: ")
        set.foreach(x => println(x.getSimpleName))
        println()

        set.asInstanceOf[Set[Class[T]]]
    }
}