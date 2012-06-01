package org.objectify.adapters

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.objectify.ObjectifySugar
import org.scalatra.ScalatraFilter


/**
  * Testing the Scalatra adapter
  */
class ScalatraAdapterTest extends WordSpec with BeforeAndAfterEach with MockitoSugar with ObjectifySugar with ShouldMatchers with ScalatraSuite {

    val scalatrafied = new ScalatraAdapter with ScalatraFilter {
        get("/test") {
            "win"
        }
    }

    override def beforeEach() {
        addFilter(scalatrafied, "/*")

        scalatrafied.actions resource ("pictures")
        scalatrafied.bootstrap()
    }

    "Scalatra adapter" should {
        "do control" in {
            get("/test") {
                status should equal(200)
                body should include("win")
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
                body should include("show")
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
}
