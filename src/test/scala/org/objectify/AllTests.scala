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
import org.junit.runners.Suite
import org.objectify.adapters.ObjectifyScalatraAdapterTest
import org.objectify.executor.{InjectorTest, ObjectifyPipelineTest}

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
