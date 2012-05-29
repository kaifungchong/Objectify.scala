package org.objectify.executor

import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.objectify.{Action, Objectify}
import org.objectify.services.Service
import org.objectify.Verb.{DELETE, PUT, POST, GET}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.PrintWriter
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.objectify.responders.{BadPolicyResponder, Responder}
import org.objectify.policies.{AuthenticationPolicy, BadPolicy, GoodPolicy, Policy}

/**
 * Testing the pipeline and sub-methods
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class ObjectifyPipelineTest extends WordSpec with BeforeAndAfterEach with MockitoSugar {
  val objectify = Objectify()
  val pipeline = new ObjectifyPipeline(objectify)

  val req = mock[HttpServletRequest]
  val res = mock[HttpServletResponse]
  var writer = mock[PrintWriter]
  var output = ""

  override protected def beforeEach() {
    objectify.defaults policy classOf[Policy]

    objectify.actions resource("pictures",
      index = Some(Action(GET, "index",
        policies = Some(List(classOf[GoodPolicy], classOf[BadPolicy])),
        service = Some(classOf[Service]),
        responder = Some(classOf[Responder]))
      ))

    // mock HTTP request methods
    when(req.getMethod).thenReturn("GET")
    when(req.getContextPath).thenReturn("/pictures")
    when(req.getServletPath).thenReturn("")
    when(req.getPathInfo).thenReturn("")

    // reset writer and output for testing
    output = ""
    writer = mock[PrintWriter]

    when(res.getWriter).thenReturn(writer)
    doAnswer(new Answer[Unit]() {
      def answer(p: InvocationOnMock) {
        output = p.getArguments.apply(0).asInstanceOf[String]
      }
    }).when(writer).println(anyString())
  }

  "The path mapper" should {
    "return the correct action for index" in {
      val route = pipeline.matchUrlToRoute("/pictures", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures")))
    }
    "return the correct action for show" in {
      val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/:id")))
    }
    "return the correct action for new" in {
      val route = pipeline.matchUrlToRoute("/pictures/new", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/new")))
    }
    "return the correct action for create" in {
      val route = pipeline.matchUrlToRoute("/pictures", objectify.actions.actions(POST))
      assert(route.equals(objectify.actions.actions(POST).get("pictures")))
    }
    "return the correct action for edit" in {
      val route = pipeline.matchUrlToRoute("/pictures/12/edit", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/:id/edit")))
    }
    "return the correct action for update" in {
      val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(PUT))
      assert(route.equals(objectify.actions.actions(PUT).get("pictures/:id")))
    }
    "return the correct action for delete" in {
      val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(DELETE))
      assert(route.equals(objectify.actions.actions(DELETE).get("pictures/:id")))
    }
  }

  "The handle method" should {
    "execute policies fail" in {
      // do the method call
      pipeline.handleRequest(req, res)

      // verify it worked
      assert(output.equals(new BadPolicyResponder()(None)))
      verify(writer).println(anyString())
    }
    "execute policies pass with resolver" in {
      objectify.actions resource("pictures",
        index = Some(Action(GET, "index",
          policies = Some(List(classOf[GoodPolicy], classOf[AuthenticationPolicy])),
          service = Some(classOf[Service]),
          responder = Some(classOf[Responder]))
        ))

      // do the method call
      pipeline.handleRequest(req, res)

      // verify it worked
      assert(output.equals(""))
      verify(writer, times(0)).println(anyString())
    }
  }
}
