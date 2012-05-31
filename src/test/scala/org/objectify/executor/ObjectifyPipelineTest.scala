package org.objectify.executor

import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.scalatest.mock.MockitoSugar
import org.objectify.HttpMethod._
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.PrintWriter
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.objectify.policies.{AuthenticationPolicy, BadPolicy, GoodPolicy, Policy}
import org.objectify.{ObjectifySugar, Action, Objectify}
import org.objectify.services.PicturesIndexService
import org.objectify.responders.{PicturesIndexResponder, BadPolicyResponder}
import org.scalatest.matchers.ShouldMatchers

/**
  * Testing the pipeline and sub-methods
  */
class ObjectifyPipelineTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers {
    val objectify = Objectify()
    val pipeline = new ObjectifyPipeline(objectify)

    val req = mock[HttpServletRequest]
    val res = mock[HttpServletResponse]
    var writer = mock[PrintWriter]
    var output = ""

    override protected def beforeEach() {
        objectify.defaults policy ~:[Policy]

        objectify.actions resource("pictures",
            index = Some(Action(Get, "index",
                policies = Some(Map(
                    ~:[GoodPolicy] -> ~:[BadPolicyResponder],
                    ~:[BadPolicy] -> ~:[BadPolicyResponder])
                ),
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]))
            ))

        // mock HTTP request methods
        when(req.getMethod).thenReturn("Get")
        when(req.getContextPath).thenReturn("/pictures")
        when(req.getServletPath).thenReturn("")
        when(req.getPathInfo).thenReturn("")

        // reset writer and output for testing
        output = ""
        writer = mock[PrintWriter]

        when(res.getWriter).thenReturn(writer)
        doAnswer(new Answer[Unit]() {
            def answer(p: InvocationOnMock) {
                output = p.getArguments.apply(0).toString
            }
        }).when(writer).println(any[Any]())
    }

    "The path mapper" should {
        "return the correct action for index" in {
            val route = pipeline.matchUrlToRoute("/pictures", objectify.actions.actions(Get))
            assert(route.equals(objectify.actions.actions(Get).get("pictures")))
        }
        "return the correct action for show" in {
            val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(Get))
            assert(route.equals(objectify.actions.actions(Get).get("pictures/:id")))
        }
        "return the correct action for new" in {
            val route = pipeline.matchUrlToRoute("/pictures/new", objectify.actions.actions(Get))
            assert(route.equals(objectify.actions.actions(Get).get("pictures/new")))
        }
        "return the correct action for create" in {
            val route = pipeline.matchUrlToRoute("/pictures", objectify.actions.actions(Post))
            assert(route.equals(objectify.actions.actions(Post).get("pictures")))
        }
        "return the correct action for edit" in {
            val route = pipeline.matchUrlToRoute("/pictures/12/edit", objectify.actions.actions(Get))
            assert(route.equals(objectify.actions.actions(Get).get("pictures/:id/edit")))
        }
        "return the correct action for update" in {
            val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(Put))
            assert(route.equals(objectify.actions.actions(Put).get("pictures/:id")))
        }
        "return the correct action for delete" in {
            val route = pipeline.matchUrlToRoute("/pictures/12", objectify.actions.actions(Delete))
            route should equal(objectify.actions.actions(Delete).get("pictures/:id"))
        }
    }

    "The handle method" should {
        "execute policies fail" in {
            // do the method call
            pipeline.handleRequest(req, res)

            // verify it worked
            assert(output.equals(new BadPolicyResponder()()))
            verify(writer).println(any[Any]())
        }

        "execute policies pass with resolver" in {
            objectify.actions resource("pictures",
                index = Some(Action(Get, "index",
                    policies = Some(Map(
                        ~:[GoodPolicy] -> ~:[BadPolicyResponder],
                        ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
                    ),
                    service = Some(~:[PicturesIndexService]),
                    responder = Some(~:[PicturesIndexResponder]))
                ))

            // do the method call
            pipeline.handleRequest(req, res)

            // verify it worked
            assert(output.equals(""))
            verify(writer, times(0)).println(anyString())
        }
    }
}
