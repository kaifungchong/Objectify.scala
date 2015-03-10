/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.adapters

import org.junit.runner.RunWith
import org.objectify.ContentType._
import org.objectify.HttpMethod._
import org.objectify.policies.{AuthenticationPolicy, GoodPolicy}
import org.objectify.responders.{BadPolicyResponder, PicturesIndexResponder}
import org.objectify.services.{Throws403, ThrowsBadRequest, ThrowsConfig, ThrowsUnexpected}
import org.objectify.{Action, ObjectifySugar}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatra.ScalatraFilter
import org.scalatra.test.scalatest.ScalatraSuite


/**
 * Testing the Scalatra adapter
 */
@RunWith(classOf[JUnitRunner])
class ObjectifyScalatraAdapterTest
  extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with Matchers with ScalatraSuite {

  val scalatrafied = new ObjectifyScalatraAdapter with ScalatraFilter {
    get("/test") {
      "win"
    }
  }

  override def beforeEach() {
    addFilter(scalatrafied, "/*")

    scalatrafied.actions resource "pictures"
    scalatrafied.bootstrap()
  }

  "The Scalatra adapter" when {

    "doing happy path cases" should {

      "do control" in {
        get("/test") {
          status should equal(200)
          body should include("win")
        }
        get("/asdfqwerty") {
          status should equal(404)
        }
      }
      "get pictures index" in {
        get("/pictures") {
          status should equal(200)
          body should include("index")
        }
      }
      "get pictures show" in {
        get("/pictures/12") {
          status should equal(200)
          body should include("show 12")
        }
      }
      "post pictures create" in {
        post("/pictures") {
          status should equal(200)
          body should include("create")
        }
      }
      "put pictures update" in {
        put("/pictures/12") {
          status should equal(200)
          body should include("update")
        }
      }
      "delete pictures destroy" in {
        delete("/pictures/12") {
          status should equal(200)
          body should include("destroy")
        }
      }
    }

    "checking content type" should {
      "return JSON by default" in {
        get("/pictures") {
          header("Content-Type") should include("application/json")
        }
      }
    }

    "doing exceptional cases" should {
      "return a 400 code for bad request" in {
        scalatrafied.actions resource("pictures", index = Some(Action(Get, "index",
          policies = Some(Map(
            ~:[GoodPolicy] -> ~:[BadPolicyResponder],
            ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
          ),
          service = Some(~:[ThrowsBadRequest]),
          responder = Some(~:[PicturesIndexResponder]))
        ))
        scalatrafied.bootstrap()

        get("/pictures") {
          status should equal(400)
        }
      }
      "return a 500 code for unexpected exceptions" in {
        scalatrafied.actions resource("pictures", index = Some(Action(Get, "index",
          policies = Some(Map(
            ~:[GoodPolicy] -> ~:[BadPolicyResponder],
            ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
          ),
          service = Some(~:[ThrowsUnexpected]),
          responder = Some(~:[PicturesIndexResponder]))
        ))
        scalatrafied.bootstrap()

        get("/pictures") {
          status should equal(500)
        }
      }
      "return a 500 code for config error" in {
        scalatrafied.actions resource("pictures", index = Some(Action(Get, "index",
          policies = Some(Map(
            ~:[GoodPolicy] -> ~:[BadPolicyResponder],
            ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
          ),
          service = Some(~:[ThrowsConfig]),
          responder = Some(~:[PicturesIndexResponder]))
        ))
        scalatrafied.bootstrap()

        get("/pictures") {
          status should equal(500)
        }
      }
      "return a custom code for custom exception" in {
        scalatrafied.actions resource("pictures", index = Some(Action(Get, "index",
          policies = Some(Map(
            ~:[GoodPolicy] -> ~:[BadPolicyResponder],
            ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder])
          ),
          service = Some(~:[Throws403]),
          responder = Some(~:[PicturesIndexResponder]))
        ))
        scalatrafied.bootstrap()

        get("/pictures") {
          status should equal(403)
        }
      }
    }

    "doing multiple format responses" should {
      "work for JSON" in {
        scalatrafied.actions action("multipleFormat", Get)
        scalatrafied.bootstrap()

        get("/multipleFormat", Nil, Map("Accept" -> JSON.toString)) {
          status should equal(200)
          header("Content-Type") should include(JSON.toString)
          body should be("some value")
        }
      }

      "work for XML" in {
        scalatrafied.actions action("multipleFormat", Get)
        scalatrafied.bootstrap()

        get("/multipleFormat", Nil, Map("Accept" -> XML.toString)) {
          status should equal(200)
          header("Content-Type") should include(XML.toString)
          body should be("some value")
        }
      }

      "default to JSON when empty" in {
        scalatrafied.actions action("multipleFormat", Get)
        scalatrafied.bootstrap()

        get("/multipleFormat") {
          status should equal(200)
          header("Content-Type") should include(JSON.toString)
          body should be("some value")
        }
      }

      "work for a custom type" in {
        scalatrafied.actions action("multipleFormat", Get)
        scalatrafied.bootstrap()

        get("/multipleFormat", Nil, Map("Accept" -> CSV.toString)) {
          status should equal(200)
          header("Content-Type") should include(CSV.toString)
          body should be("some value")
        }
      }
    }
  }
}
