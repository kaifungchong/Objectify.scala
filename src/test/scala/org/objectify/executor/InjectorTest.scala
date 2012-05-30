package org.objectify.executor

import org.objectify.ObjectifySugar
import org.scalatest.mock.MockitoSugar
import org.scalatest.{WordSpec, BeforeAndAfterEach}
import org.objectify.policies.Policy
import javax.servlet.http.HttpServletRequest
import javax.inject.Named
import org.objectify.resolvers.{CurrentUserResolver, StringResolver}
/**
 * Testing the injector
 *
 * @author Arthur Gonigberg
 * @since 12-05-29
 */
class InjectorTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar {
  val stringResolverActual = new StringResolver().apply(null)
  val currentUserResolverActual = new CurrentUserResolver().apply(null)

  "Injector" should {
    "resolve type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy1].erasure.getConstructors.head, mock[HttpServletRequest])
        .asInstanceOf[List[String]].head.equalsIgnoreCase(stringResolverActual))
    }
    "resolve annotation" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy2].erasure.getConstructors.head, mock[HttpServletRequest])
        .asInstanceOf[List[String]].head.equalsIgnoreCase(currentUserResolverActual))
    }
    "resolve type and annotation" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy3].erasure.getConstructors.head, mock[HttpServletRequest])
        .asInstanceOf[List[String]].equals(List(currentUserResolverActual, stringResolverActual)))
    }
    "resolve annotation and type" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy4].erasure.getConstructors.head, mock[HttpServletRequest])
        .asInstanceOf[List[String]].equals(List(stringResolverActual, currentUserResolverActual)))
    }
    "resolve mish mash" in {
      assert(Injector.getInjectedResolverParams(manifest[TestPolicy5].erasure.getConstructors.head, mock[HttpServletRequest])
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
  }
}

private class TestPolicy1(string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy2(@Named("CurrentUserResolver") string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy3(@Named("CurrentUserResolver") user: String, string: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy4(string: String, @Named("CurrentUserResolver") user: String) extends Policy {
  def isAllowed = true
}

private class TestPolicy5(string1: String,
                          @Named("CurrentUserResolver") user1: String,
                          @Named("CurrentUserResolver") user2: String,
                          string4: String,
                          @Named("CurrentUserResolver") user3: String,
                          string2: String,
                          string3: String,
                          @Named("CurrentUserResolver") user4: String)
  extends Policy {
  def isAllowed = true
}
