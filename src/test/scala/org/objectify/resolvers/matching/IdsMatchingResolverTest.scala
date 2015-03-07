package org.objectify.resolvers.matching

import mojolly.inflector.Inflector
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.exceptions.BadRequestException
import org.objectify.{ObjectifySugar, TestHelpers}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}

/**
 * @author Joe Gaudet - (joe@learndot.com)
 */
@RunWith(classOf[JUnitRunner])
class IdsMatchingResolverTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers with TestHelpers {

  "IdsMatchingResolver" should {

    val mockObjectifyRequest = mock[ObjectifyRequestAdapter]

    "resolve the id parameter from the query string" in {
      // setup
      val queryIds = List(anonymousId, anonymousId)
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map("ids" -> queryIds.map(_.toString)))

      // exercise
      val resolvedValue = IdsMatchingResolver("Ids")(mockObjectifyRequest)

      // assert
      resolvedValue should equal(queryIds)
    }

    "resolve an anonymous id parameter from the query string" in {
      // setup
      val queryIds = List(anonymousId, anonymousId)
      val queryName = anonymousClassName

      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map(Inflector.uncapitalize(queryName) -> queryIds.map(_.toString)))

      // exercise
      val resolvedValue = IdsMatchingResolver(queryName)(mockObjectifyRequest)

      // assert
      resolvedValue should equal(queryIds)
    }
    "resolve the id parameter from the body without a query param" in {
      // setup
      val bodyIds = List(anonymousId, anonymousInt)
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"ids": [${bodyIds.map(_.toString).mkString(",")}], "foo": "bar"}""")

      // exercise
      val resolvedValue = IdsMatchingResolver("Ids")(mockObjectifyRequest)

      // assert
      resolvedValue should equal(bodyIds)
    }

    "resolve an anonymous parameter from the body without path or query param with" in {
      // setup
      val bodyIds = List(anonymousId, anonymousInt)
      val bodyName = anonymousClassName
      val bodyKey = Inflector.uncapitalize(bodyName)
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"$bodyKey": [${bodyIds.map(_.toString).mkString(",")}], "foo": "bar"}""")

      // exercise
      val resolvedValue = IdsMatchingResolver(bodyName)(mockObjectifyRequest)

      // assert
      resolvedValue should equal(bodyIds)
    }

    "fail loudly when none are present" in {
      // setup
      val bodyId = anonymousId
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"noIdYo": $bodyId, "foo": "bar"}""")

      // exercise
      an[BadRequestException] should be thrownBy IdsMatchingResolver("Ids")(mockObjectifyRequest)
    }

  }
}
