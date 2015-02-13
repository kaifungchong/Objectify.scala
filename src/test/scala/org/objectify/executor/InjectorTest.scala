/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.executor

import org.junit.runner.RunWith
import org.objectify.policies.Policy
import org.objectify.resolvers._
import org.objectify.ObjectifySugar
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.WordSpec

import javax.inject.Named
import org.objectify.adapters.ObjectifyRequestAdapter

@RunWith(classOf[JUnitRunner])
class InjectorTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar {
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
  }
}

private class TestPolicy1(string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy2(@Named("CurrentUser") string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy3(@Named("CurrentUser") user: String, string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy4(string: String, @Named("CurrentUser") user: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy5(string1: String,
                          @Named("CurrentUser") user1: String,
                          @Named("CurrentUser") user2: String,
                          string4: String,
                          @Named("CurrentUser") user3: String,
                          string2: String,
                          string3: String,
                          @Named("CurrentUser") user4: String)
  extends Policy {
  def isAllowed = true
}

private class TestPolicyGeneric1(string: List[String], @Named("CurrentUser") user: String) extends Policy {
  def isAllowed = true
}

private class TestPolicyGeneric2(string: String, @Named("ListCurrentUser") user: List[String]) extends Policy {
  def isAllowed = true
}

private class TestPolicyGeneric3(string: List[String], @Named("ListCurrentUser") user: List[String]) extends Policy {
  def isAllowed = true
}

private class TestPolicyGeneric4(string: Option[List[String]], @Named("OptionListCurrentUser") user: Option[List[String]]) extends Policy {
  def isAllowed = true
}


