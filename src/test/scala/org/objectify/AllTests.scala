package org.objectify

import adapters.ObjectifyScalatraAdapterTest
import executor.{ObjectifyPipelineTest, InjectorTest}
import org.junit.runners.Suite
import org.junit.runner.RunWith

/**
  * Run entire test suite
  */
@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array[Class[_]](
    classOf[ObjectifyScalatraAdapterTest],
    classOf[InjectorTest],
    classOf[ObjectifyPipelineTest]
))
class AllTests {
    // nothing
}
