package org.objectify

import adapters.ObjectifyRequestAdapter
import com.yammer.metrics.scala.{Counter, Timer, Instrumented}

/**
  * Instrumented Objectify trait -- using Coda Hale's Metrics
  */
trait ObjectifyInstrumented extends Objectify with Instrumented {
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