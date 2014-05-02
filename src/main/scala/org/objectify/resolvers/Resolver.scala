/*
 * -------------------------------------------------------------------------------------------------
 * - Project:   Objectify                                                                          -
 * - Copyright: ©2014 Matygo Educational Incorporated operating as Learndot                        -
 * - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)     -
 * - License:   Licensed under MIT license (see license.txt)                                       -
 * -------------------------------------------------------------------------------------------------
 */

package org.objectify.resolvers

/**
  * Interface for resolvers. Resolvers are intended to help Objectify figure out how to inject
  * the constructors for various types in the application.
  */
trait Resolver[T, P] {
    def apply(param: P): T
}
