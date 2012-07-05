package org.objectify.executor

import org.junit.runner.RunWith
import org.objectify.policies.Policy
import org.objectify.resolvers.{ListCurrentUserResolver, ListStringResolver, CurrentUserResolver, StringResolver}
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
    val resolverParamMock = mock[ObjectifyRequestAdapter]

    "Injector" should {
        "resolve type" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicy1].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].head.equalsIgnoreCase(stringResolverActual))
        }
        "resolve annotation" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicy2].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].head.equalsIgnoreCase(currentUserResolverActual))
        }
        "resolve type and annotation" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicy3].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].equals(List(currentUserResolverActual, stringResolverActual)))
        }
        "resolve annotation and type" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicy4].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].equals(List(stringResolverActual, currentUserResolverActual)))
        }
        "resolve mish mash" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicy5].erasure.getConstructors.head, resolverParamMock)
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

        "resolve annotation and generic type" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric1].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].equals(List(listStringResolverActual, currentUserResolverActual)))
        }
        "resolve generic annotation and type" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric2].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].equals(List(stringResolverActual, listCurrentUserResolverActual)))
        }
        "resolve generic annotation and generic type" in {
            assert(Injector.getInjectedResolverParams(manifest[TestPolicyGeneric3].erasure.getConstructors.head, resolverParamMock)
                .asInstanceOf[List[String]].equals(List(listStringResolverActual, listCurrentUserResolverActual)))
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

private class TestPolicyGeneric1(string: List[String], @Named("CurrentUserResolver") user: String) extends Policy {
    def isAllowed = true
}
private class TestPolicyGeneric2(string: String, @Named("ListCurrentUserResolver") user: List[String]) extends Policy {
    def isAllowed = true
}
private class TestPolicyGeneric3(string: List[String], @Named("ListCurrentUserResolver") user: List[String]) extends Policy {
    def isAllowed = true
}

