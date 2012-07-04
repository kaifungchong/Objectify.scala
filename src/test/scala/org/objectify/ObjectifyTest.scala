package org.objectify

import adapters.ObjectifyScalatraAdapter
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

            policies should have size (7)
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

            policies.flatten should have size (6)
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

            policies should have size (7)
            policies.flatten should have size (14)
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
            filteredPols should have size (7)
            filteredPols.flatten should have size (9)
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
            filteredPols should have size (7)
            filteredPols.flatten should have size (12)
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
            filteredPols should have size (5)
            filteredPols.flatten should have size (6)
        }
    }
}

