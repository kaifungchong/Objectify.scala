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
    classOf[ObjectifyPipelineTest],
    classOf[ObjectifyTest],
    classOf[BootstrapValidationTest]
))
class AllTests {
    // nothing
}
