package org.objectify.executor

import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.objectify.{Action, Objectify}
import org.objectify.services.Service
import org.objectify.Verb.{DELETE, PUT, POST, GET}
import org.objectify.policies.{BadPolicy, GoodPolicy, Policy}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.PrintWriter
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.objectify.responders.{BadPolicyResponder, Responder}

/**
 * Testing the pipeline and sub-methods
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class ObjectifyPipelineTest extends WordSpec with BeforeAndAfterEach with MockitoSugar {
  val objectify = Objectify()
  val op = new ObjectifyPipeline(objectify)

  override protected def beforeEach() {
    objectify.defaults policy classOf[Policy]

    objectify.actions resource("pictures",
      index = Some(Action(GET, "index",
        policies = Some(List(classOf[GoodPolicy], classOf[BadPolicy])),
        service = Some(classOf[Service]),
        responder = Some(classOf[Responder]))
      ))
  }

  "The path mapper" should {
    "return the correct action for index" in {
      val route = op.matchUrlToRoute("/pictures", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures")))
    }
    "return the correct action for show" in {
      val route = op.matchUrlToRoute("/pictures/12", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/:id")))
    }
    "return the correct action for new" in {
      val route = op.matchUrlToRoute("/pictures/new", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/new")))
    }
    "return the correct action for create" in {
      val route = op.matchUrlToRoute("/pictures", objectify.actions.actions(POST))
      assert(route.equals(objectify.actions.actions(POST).get("pictures")))
    }
    "return the correct action for edit" in {
      val route = op.matchUrlToRoute("/pictures/12/edit", objectify.actions.actions(GET))
      assert(route.equals(objectify.actions.actions(GET).get("pictures/:id/edit")))
    }
    "return the correct action for update" in {
      val route = op.matchUrlToRoute("/pictures/12", objectify.actions.actions(PUT))
      assert(route.equals(objectify.actions.actions(PUT).get("pictures/:id")))
    }
    "return the correct action for delete" in {
      val route = op.matchUrlToRoute("/pictures/12", objectify.actions.actions(DELETE))
      assert(route.equals(objectify.actions.actions(DELETE).get("pictures/:id")))
    }
  }

  "The handle method" should {
    "execute policies" in {
      val req = mock[HttpServletRequest]
      val res = mock[HttpServletResponse]

      when(req.getMethod).thenReturn("GET")
      when(req.getContextPath).thenReturn("/pictures")
      when(req.getServletPath).thenReturn("")
      when(req.getPathInfo).thenReturn("")

      // mock out the response writer
      val writer = mock[PrintWriter]
      var output = ""
      when(res.getWriter).thenReturn(writer)
      doAnswer(new Answer[Unit]() {
        def answer(p: InvocationOnMock) {
          output = p.getArguments.apply(0).asInstanceOf[String]
        }
      }).when(writer).println(anyString())

      // do the method call
      op.handleRequest(req, res)

      // verify it worked
      assert(output.equals(new BadPolicyResponder()()))
      verify(writer).println(anyString())
    }
  }
}
