/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify

import org.junit.runner.RunWith
import org.objectify.HttpMethod._
import org.objectify.adapters.ObjectifyScalatraAdapter
import org.objectify.exceptions.ConfigurationException
import org.objectify.responders.ServiceResponder
import org.objectify.services.PicturesIndexService
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatra.ScalatraFilter
import org.scalatra.test.scalatest.ScalatraSuite

/**
 * Making sure bootstrap validation works correctly
 */
@RunWith(classOf[JUnitRunner])
class BootstrapValidationTest
  extends WordSpec with Matchers with BeforeAndAfterEach with ScalatraSuite with ObjectifySugar with MockitoSugar {

  val scalatrafied = new ObjectifyScalatraAdapter with ScalatraFilter {
    get("/test") {
      "win"
    }
  }

  override def beforeEach() {
    addFilter(scalatrafied, "/*")
  }

  "Bootstrap validation" should {
    "pass the control test" in {
      scalatrafied.actions resource "pictures"
      scalatrafied.bootstrap()

      get("/test") {
        status should be(200)
        body should be("win")
      }
    }
    "Fail when a service doesn't exist" in {
      scalatrafied.actions actionBase(Get, "asdf", "asdf",
        policies = None,
        service = None,
        responder = None
        )
      val thrown = the[ConfigurationException] thrownBy scalatrafied.bootstrap()
      thrown.getMessage should equal("No class matching the name: AsdfService")
    }


    "Pass when responders don't exist" in {
      scalatrafied.actions actionBase(Get, "asdf", "asdf",
        policies = None,
        service = -:[PicturesIndexService],
        responder = None
        )
    }


    "Fail when responders don't match up with services" in {
      scalatrafied.actions actionBase(Get, "asdf", "asdf",
        policies = None,
        service = -:[PicturesIndexService],
        responder = -:[NonStringResponder]
        )
      val thrown = the[ConfigurationException] thrownBy scalatrafied.bootstrap()
      val message = "Service [class org.objectify.services.PicturesIndexService] and " +
        "Responder [class org.objectify.NonStringResponder] are not compatible. Service return " +
        "type [class java.lang.String] does not match Responder apply method parameter [public java.lang.String org.objectify.NonStringResponder.apply(int)]."

      thrown.getMessage should equal(message)
    }
  }
}

class NonStringResponder extends ServiceResponder[String, Int] {
  def apply(serviceResult: Int) = serviceResult.toString
}
