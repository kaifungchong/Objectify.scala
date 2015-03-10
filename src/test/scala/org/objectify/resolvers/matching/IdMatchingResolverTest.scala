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
class IdMatchingResolverTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers with TestHelpers {

  "IdMatchingResolver" should {

    val mockObjectifyRequest = mock[ObjectifyRequestAdapter]


    "resolve the id parameter from the path" in {
      // setup
      val pathId = anonymousId
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map("id" -> pathId.toString))
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())

      // exercise
      val resolvedValue = IdMatchingResolver("Id")(mockObjectifyRequest)

      // assert
      resolvedValue should be(pathId)
    }

    "resolve an anonymous id parameter from the path with an anonymous id name" in {
      // setup
      val pathId = anonymousId
      val pathName = anonymousClassName
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map(Inflector.uncapitalize(pathName) -> pathId.toString))
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())

      // exercise
      val resolvedValue = IdMatchingResolver(pathName)(mockObjectifyRequest)

      // assert
      resolvedValue should be(pathId)
    }

    "resolve the id parameter from the query string when the path parameter is missing" in {
      // setup
      val queryId = anonymousId
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map("id" -> List(queryId.toString)))

      // exercise
      val resolvedValue = IdMatchingResolver("Id")(mockObjectifyRequest)

      // assert
      resolvedValue should be(queryId)
    }

    "resolve parameter from the query string with an anonymous id name when the path parameter is missing" in {
      // setup
      val queryId = anonymousId
      val queryName = anonymousClassName
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map(Inflector.uncapitalize(queryName) -> List(queryId.toString)))

      // exercise
      val resolvedValue = IdMatchingResolver(queryName)(mockObjectifyRequest)

      // asert
      resolvedValue should be(queryId)
    }


    "resolve the id parameter from the body without path or query param" in {
      // setup
      val bodyId = anonymousId
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"id": $bodyId, "foo": "bar"}""")

      // exercise
      val resolvedValue = IdMatchingResolver("Id")(mockObjectifyRequest)

      // assert
      resolvedValue should be(bodyId)
    }

    "resolve an anonymous parameter from the body without path or query param with" in {
      // setup
      val bodyId = anonymousId
      val bodyName = anonymousClassName
      val bodyKey = Inflector.uncapitalize(bodyName)
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"$bodyKey": $bodyId, "foo": "bar"}""")

      // exercise
      val resolvedValue = IdMatchingResolver(bodyName)(mockObjectifyRequest)

      // assert
      resolvedValue should be(bodyId)
    }

    "fail loudly when none are present" in {
      // setup
      val bodyId = anonymousId
      when(mockObjectifyRequest.getPathParameters).thenReturn(Map[String, String]())
      when(mockObjectifyRequest.getQueryParameters).thenReturn(Map[String, List[String]]())
      when(mockObjectifyRequest.getBody).thenReturn( s"""{"noIdYo": $bodyId, "foo": "bar"}""")

      // exercise
      an[BadRequestException] should be thrownBy IdMatchingResolver("Id")(mockObjectifyRequest)
    }

  }
}
