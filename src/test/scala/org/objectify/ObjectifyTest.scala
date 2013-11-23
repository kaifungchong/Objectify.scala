/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify

import adapters.ObjectifyScalatraAdapter
import HttpMethod._
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatra.ScalatraFilter
import policies.{AuthenticationPolicy, GoodPolicy}
import responders.BadPolicyResponder

/**
  * Testing objectify syntax
  */
@RunWith(classOf[JUnitRunner])
class ObjectifyTest extends WordSpec with ShouldMatchers with ObjectifyImplicits with ObjectifySugar {

    class FakeObjectifyFilter extends ObjectifyScalatraAdapter with ScalatraFilter

    "Objectify syntax" should {
        "apply policy to all actions" in {
            val objf = new FakeObjectifyFilter
            val tuple = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("pictures") policy tuple

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.get.head._1

            policies should have size (5)
            policies.head should be(tuple._1)
        }
        "apply policy to only given actions" in {
            val objf = new FakeObjectifyFilter
            val tuple = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("pictures") policy (tuple only("index", "show"))

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.map(_.head._1)

            policies.flatten should have size (2)
            policies.head.get should be(tuple._1)
        }
        "apply policy to all except given actions" in {
            val objf = new FakeObjectifyFilter
            val tuple = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("catz") policy (tuple except ("destroy"))

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.map(_.head._1)

            policies.flatten should have size (4)
            policies.head.get should be(tuple._1)
        }
        "apply policies to all actions" in {
            val objf = new FakeObjectifyFilter
            val tuple1 = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            val tuple2 = ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("catz") policies(tuple1, tuple2)

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.get.keys

            policies should have size (5)
            policies.flatten should have size (10)
            policies.head should equal(Set(tuple1._1, tuple2._1))
        }
        "apply policies to all actions and only actions" in {
            val objf = new FakeObjectifyFilter
            val tuple1 = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            val tuple2 = ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("catz") policies(tuple1, tuple2 only("index", "show"))

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.map(_.keys)

            val filteredPols = policies.flatten
            filteredPols should have size (5)
            filteredPols.flatten should have size (7)
        }
        "apply policies to all actions and except actions" in {
            val objf = new FakeObjectifyFilter
            val tuple1 = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            val tuple2 = ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("catz") policies(tuple1, tuple2 except("index", "show"))

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.map(_.keys)

            val filteredPols = policies.flatten
            filteredPols should have size (5)
            filteredPols.flatten should have size (8)
        }
        "apply policies to only actions and except actions" in {
            val objf = new FakeObjectifyFilter
            val tuple1 = ~:[GoodPolicy] -> ~:[BadPolicyResponder]
            val tuple2 = ~:[AuthenticationPolicy] -> ~:[BadPolicyResponder]
            objf.actions resource ("catz") policies(tuple1 only("destroy"), tuple2 except("index", "show"))

            val policies = for {(method, action) <- objf.actions.actions
                                (strAction, realAction) <- action}
            yield realAction.policies.map(_.keys)

            val filteredPols = policies.flatten
            filteredPols should have size (3)
            filteredPols.flatten should have size (4)
        }

        "shorthand for only certain routes on a resource" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") only "index"

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield realAction.name

            actionNames should be(List("CatsIndex"))
        }
        "shorthand for only certain routes on a resource 2" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") only ("index", "create")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield realAction.name

            actionNames should be(List("CatsIndex", "CatsCreate"))
        }
        "shorthand for only certain routes on a resource default routes" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") only ("index", "create", "show")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("CatsIndex" -> Some("cats"), "CatsCreate" -> Some("cats"), "CatsShow" -> Some("cats/:id")))
        }
        "shorthand for only certain routes on a resource defined routes" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") onlyRoute ("index" -> "kitty", "create" -> "kitty", "show" -> "kitty")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("CatsIndex" -> Some("kitty"), "CatsCreate" -> Some("kitty"), "CatsShow" -> Some("kitty")))
        }

        "shorthand for except certain routes on a resource" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") except "index"

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield realAction.name

            actionNames should have size(4)
        }
        "shorthand for except certain routes on a resource 2" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") except ("index", "create")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield realAction.name

            actionNames should have size(3)
        }
        "shorthand for except certain routes on a resource default routes" in {
            val objf = new FakeObjectifyFilter
            objf.actions resource ("cats") except ("index", "create", "show")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("CatsDestroy" -> Some("cats/:id"), "CatsUpdate" -> Some("cats/:id")))
        }

        "method to add to actions to a resource" in {
            val objf = new FakeObjectifyFilter
            val resource = objf.actions resource ("cats") except ("index", "create", "show")

            resource action ("duplicate")

            resource action ("duplicate", Post)

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("CatsDestroy" -> Some("cats/:id"), "CatDuplicateGet" -> Some("cats/:id/duplicate"),
                "CatDuplicatePost" -> Some("cats/:id/duplicate"), "CatsUpdate" -> Some("cats/:id")))
        }

        "method to add to actions to a resource with index" in {
            val objf = new FakeObjectifyFilter
            val resource = objf.actions resource ("cats") except ("index", "create", "show")

            resource action ("duplicate")

            resource action ("duplicate", Post)

            resource action ("grouped", isIndex = true)

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("CatsDestroy" -> Some("cats/:id"), "CatDuplicateGet" -> Some("cats/:id/duplicate"),
                "CatDuplicatePost" -> Some("cats/:id/duplicate"), "CatsGroupedGet" -> Some("cats/grouped"),
                "CatsUpdate" -> Some("cats/:id")))
        }

        "method to add to actions as a resource" in {
            val objf = new FakeObjectifyFilter

            objf.actions action ("duplicate")
            objf.actions action ("duplicate", Post)
            objf.actions action ("duplicate", Put, "asdf/:id/asdfasdf")

            val actionNames = for {(method, action) <- objf.actions.actions
                                   (strAction, realAction) <- action}
            yield (realAction.name, realAction.route)

            actionNames should be(Map("DuplicateGet" -> Some("duplicate"), "DuplicatePost" -> Some("duplicate"),
                "DuplicatePut" -> Some("asdf/:id/asdfasdf")))
        }
    }
}

