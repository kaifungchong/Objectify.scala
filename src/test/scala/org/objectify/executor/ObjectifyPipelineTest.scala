package org.objectify.executor

import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.objectify.{Action, Objectify}
import org.objectify.services.Service
import org.objectify.responders.Responder
import org.objectify.Verb.{DELETE, PUT, POST, GET}
import org.objectify.policies.{GoodPolicy, Policy}

/**
 * Testing the pipeline and sub-methods
 *
 * @author Arthur Gonigberg
 * @since 12-05-25
 */
class ObjectifyPipelineTest extends WordSpec with BeforeAndAfterEach {
  val objectify = Objectify()
  val op = new ObjectifyPipeline(objectify)

  override protected def beforeEach() {
    objectify.defaults policy classOf[Policy]

    objectify.actions resource("pictures",
      index = Some(Action(GET, "index",
        policies = Some(List(Class[GoodPolicy])),
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
}
