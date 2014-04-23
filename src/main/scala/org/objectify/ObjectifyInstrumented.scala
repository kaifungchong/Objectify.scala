/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify

import adapters.ObjectifyRequestAdapter
import nl.grons.metrics.scala.{InstrumentedBuilder, Counter, Timer}

/**
 * Instrumented Objectify trait -- using Coda Hale's Metrics
 */
trait ObjectifyInstrumented extends Objectify with InstrumentedBuilder {
    private val bootstrapTimer = metrics.timer("BootstrapTimer")
    private val miscTimer = metrics.timer("timer.actionRoute.undefined")
    private val miscCounter = metrics.counter("counter.actionRoute.undefined")

    private var actionTimerMap = Map[String, Timer]()

    private var actionCounterMap = Map[String, Counter]()

    override def bootstrap() {
        bootstrapTimer.time {
            super.bootstrap()
        }

        // after bootstrap, create a timer and counter for each action
        actionTimerMap = for {
            (method, actionMap) <- actions.actions
            (route, action) <- actionMap
        } yield (route, metrics.timer("timer.actionRoute." + route))

        actionCounterMap = for {
            (method, actionMap) <- actions.actions
            (route, action) <- actionMap
        } yield (route, metrics.counter("counter.actionRoute." + route))
    }

    override def execute(action: Action, requestAdapter: ObjectifyRequestAdapter) = {
        val timer = actionTimerMap.get(action.route.getOrElse("")).getOrElse(miscTimer)
        val counter = actionCounterMap.get(action.route.getOrElse("")).getOrElse(miscCounter)

        counter.+=(1)
        timer.time {
            super.execute(action, requestAdapter)
        }
    }
}
