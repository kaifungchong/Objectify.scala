package org.objectify.adapters

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.scalatra.ScalatraFilter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.objectify.{Action, ObjectifySugar}
import org.objectify.HttpMethod._
import org.objectify.responders.{PicturesIndexResponder, BadPolicyResponder}
import org.objectify.policies.{AuthenticationPolicy, GoodPolicy}
import org.objectify.services.{Throws403, ThrowsConfig, ThrowsBadRequest}


/**
  * Testing the Scalatra adapter
  */
@RunWith(classOf[JUnitRunner])
class ObjectifyScalatraAdapterTest
    extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers with ScalatraSuite {

    val scalatrafied = new ObjectifyScalatraAdapter with ScalatraFilter {
        get("/test") {
            "win"
        }
    }

    override def beforeEach() {
        addFilter(scalatrafied, "/*")

        scalatrafied.actions resource ("pictures")
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
            "get pictures new" in {
                get("/pictures/new") {
                    status should equal(200)
                    body should include("new")
                }
            }
            "post pictures create" in {
                post("/pictures") {
                    status should equal(200)
                    body should include("create")
                }
            }
            "get pictures edit" in {
                get("/pictures/12/edit") {
                    status should equal(200)
                    body should include("edit")
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
    }
}
