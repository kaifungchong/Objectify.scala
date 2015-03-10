/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import javax.inject.Named

import com.twitter.logging.{Level, Logger}
import org.junit.runner.RunWith
import org.objectify.ObjectifySugar
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.policies.Policy
import org.objectify.resolvers._
import org.objectify.resolvers.matching.IdMatchingResolver
import org.scalatest.{ShouldMatchers, BeforeAndAfterEach, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._


@RunWith(classOf[JUnitRunner])
class InjectorTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers {

  val logger = Logger("")
  logger.setLevel(Level.TRACE)

  val stringResolverActual = new StringResolver().apply(null)
  val currentUserResolverActual = new CurrentUserResolver().apply(null)
  val listStringResolverActual = new ListStringResolver().apply(null)
  val listCurrentUserResolverActual = new ListCurrentUserResolver().apply(null)
  val optionListStringResolverActual = new OptionListStringResolver().apply(null)
  val optionListCurrentUserResolverActual = new OptionListCurrentUserResolver().apply(null)


  val resolverParamMock = mock[ObjectifyRequestAdapter]


  "Injector" should {
    "resolve type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy1].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].head.equalsIgnoreCase(stringResolverActual))
    }
    "resolve named annotation" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy2].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].head.equalsIgnoreCase(currentUserResolverActual))
    }
    "resolve type and named annotation" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy3].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(List(currentUserResolverActual, stringResolverActual)))
    }
    "resolve name annotation and type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy4].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(List(stringResolverActual, currentUserResolverActual)))
    }
    "resolve mish mash" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy5].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(
          List(
            stringResolverActual,
            currentUserResolverActual,
            currentUserResolverActual,
            stringResolverActual,
            currentUserResolverActual,
            stringResolverActual,
            stringResolverActual,
            currentUserResolverActual
          )))
    }

    "resolve named annotation and generic type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric1].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(List(listStringResolverActual, currentUserResolverActual)))
    }
    "resolve generic named annotation and type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric2].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(List(stringResolverActual, listCurrentUserResolverActual)))
    }

    "resolve generic named annotation and generic type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric3].runtimeClass.getConstructors.head, resolverParamMock)
        .asInstanceOf[List[String]].equals(List(listStringResolverActual, listCurrentUserResolverActual)))
    }

    "resolve multi generic annotation and generic type" in {
      val params = Injector.getInjectedResolverParams(manifest[TestPolicyGeneric4].runtimeClass.getConstructors.head, resolverParamMock)
      assert(params.asInstanceOf[List[Option[List[String]]]].equals(List(optionListStringResolverActual, optionListCurrentUserResolverActual)))
    }

    "resolve named match params" in {
      val idResolverParamMock = mock[ObjectifyRequestAdapter]
      when(idResolverParamMock.getPathParameters).thenReturn(Map("id" -> "1"))
      when(idResolverParamMock.getQueryParameters).thenReturn(Map[String, List[String]]())
      val idResolverActual = new IdMatchingResolver("Id").apply(idResolverParamMock)

      val resolvedParam = Injector.getInjectedResolverParams(manifest[TestPolicyWithGenericId].runtimeClass.getConstructors.head, idResolverParamMock)
      resolvedParam should equal(List(idResolverActual))
    }

    "resolve named match params with specific name" in {

      val idResolverParamMock = mock[ObjectifyRequestAdapter]
      when(idResolverParamMock.getPathParameters).thenReturn(Map("courseId" -> "1"))
      when(idResolverParamMock.getQueryParameters).thenReturn(Map[String, List[String]]())
      val idResolverActual = new IdMatchingResolver("courseId").apply(idResolverParamMock)

      val resolvedParam = Injector.getInjectedResolverParams(manifest[TestPolicyWithGenericCourseId].runtimeClass.getConstructors.head, idResolverParamMock)
      resolvedParam should equal(List(idResolverActual))
    }
  }
}


private class UnityPolicy extends Policy {
  override def isAllowed = true
}

private class TestPolicy1(string: String) extends UnityPolicy

private class TestPolicy2(@Named("CurrentUser") string: String) extends UnityPolicy

private class TestPolicy3(@Named("CurrentUser") user: String, string: String) extends UnityPolicy

private class TestPolicy4(string: String, @Named("CurrentUser") user: String) extends UnityPolicy

private class TestPolicy5(string1: String,
                          @Named("CurrentUser") user1: String,
                          @Named("CurrentUser") user2: String,
                          string4: String,
                          @Named("CurrentUser") user3: String,
                          string2: String,
                          string3: String,
                          @Named("CurrentUser") user4: String)
  extends UnityPolicy

private class TestPolicyGeneric1(string: List[String], @Named("CurrentUser") user: String) extends UnityPolicy

private class TestPolicyGeneric2(string: String, @Named("ListCurrentUser") user: List[String]) extends UnityPolicy

private class TestPolicyGeneric3(string: List[String], @Named("ListCurrentUser") user: List[String]) extends UnityPolicy

private class TestPolicyGeneric4(string: Option[List[String]], @Named("OptionListCurrentUser") user: Option[List[String]]) extends UnityPolicy

private class TestPolicyWithGenericId(@Named("Id") id: Int) extends UnityPolicy

private class TestPolicyWithGenericCourseId(@Named("courseId") courseId: Int) extends UnityPolicy

private class TestPolicyWithGenericIds(@Named("Ids") id: List[Int]) extends UnityPolicy

private class TestPolicyWithGenericCourseIds(@Named("courseIds") id: List[Int]) extends UnityPolicy
