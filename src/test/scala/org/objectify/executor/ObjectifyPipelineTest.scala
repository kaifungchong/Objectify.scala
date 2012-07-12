package org.objectify.executor

import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.scalatest.mock.MockitoSugar
import org.objectify.HttpMethod._
import org.mockito.Mockito._
import org.objectify.policies.{AuthenticationPolicy, BadPolicy, GoodPolicy}
import org.objectify.services.PicturesIndexService
import org.objectify.responders.{PicturesIndexResponder, BadPolicyResponder}
import org.scalatest.matchers.ShouldMatchers
import org.objectify.adapters.ObjectifyRequestAdapter
import org.objectify.{ObjectifySugar, Action, Objectify}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
  * Testing the pipeline and sub-methods
  */
@RunWith(classOf[JUnitRunner])
class ObjectifyPipelineTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers {
    val objectify = Objectify()
    val pipeline = new ObjectifyPipeline(objectify)

    val req = mock[ObjectifyRequestAdapter]
    var action = Some(Action(Get, "index",
        policies = Some(Map(
            ~:[GoodPolicy] -> ~:[BadPolicyResponder],
            ~:[BadPolicy] -> ~:[BadPolicyResponder])
        ),
        service = Some(~:[PicturesIndexService]),
        responder = Some(~:[PicturesIndexResponder])))

    override protected def beforeEach() {
        objectify.defaults policy ~:[GoodPolicy] -> ~:[BadPolicyResponder]

        objectify.actions resource("pictures", index = action)

        // mock HTTP request methods
        when(req.getHttpMethod).thenReturn(Get)
        when(req.getPath).thenReturn("/pictures")

        super.beforeEach()
    }

    override protected def afterEach() {
        objectify.defaults.globalPolicies = Map()
        objectify.defaults.defaultPolicies = Map()
        super.afterEach()
    }

    "The handle method" should {
        "execute policies fail" in {
            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal(new BadPolicyResponder()())
        }

        "execute policies pass with resolver" in {
            action = Some(Action(Get, "index",
                policies = Some(Map(
                    ~:[GoodPolicy] -> ~:[BadPolicyResponder],
                    ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
                ),
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]))
            )

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal("index")
        }

        "execute global and local policies" in {
            objectify.defaults globalPolicy ~:[BadPolicy] -> ~:[BadPolicyResponder]

            action = Some(Action(Get, "index",
                policies = Some(Map(~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])),
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]))
            )

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal(new BadPolicyResponder()())
        }

        "execute global and local policies 2" in {
            objectify.defaults globalPolicy ~:[GoodPolicy] -> ~:[BadPolicyResponder]

            action = Some(Action(Get, "index",
                policies = Some(Map(~:[BadPolicy] -> ~:[BadPolicyResponder])),
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]))
            )

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal(new BadPolicyResponder()())
        }

        "execute global and default policies" in {
            objectify.defaults globalPolicy ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            objectify.defaults policy ~:[BadPolicy] -> ~:[BadPolicyResponder]

            action = Some(Action(Get, "index",
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder])))

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal(new BadPolicyResponder()())
        }

        "ignore global and execute default policies" in {
            objectify.defaults globalPolicy ~:[BadPolicy] -> ~:[BadPolicyResponder]
            objectify.defaults policy ~:[GoodPolicy] -> ~:[BadPolicyResponder]

            action = Some(Action(Get, "index",
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]),
                ignoreGlobalPolicies = true))

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal("index")
        }

        "ignore global and execute local policies" in {
            objectify.defaults globalPolicy ~:[BadPolicy] -> ~:[BadPolicyResponder]

            action = Some(Action(Get, "index",
                policies = Some(Map(~:[GoodPolicy] -> ~:[BadPolicyResponder])),
                service = Some(~:[PicturesIndexService]),
                responder = Some(~:[PicturesIndexResponder]),
                ignoreGlobalPolicies = true))

            // do the method call
            val response = pipeline.handleRequest(action.get, req)

            // verify it worked
            response.getSerializedEntity should equal("index")
        }
    }
}
