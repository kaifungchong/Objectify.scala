/*
 * -------------------------------------------------------------------------------------------------
 *  - Project:   Objectify                                                                           -
 *  - Copyright: ©2013 Matygo Educational Incorporated operating as Learndot                         -
 *  - Author:    Arthur Gonigberg (arthur@learndot.com) and contributors (see contributors.txt)      -
 *  - License:   Licensed under MIT license (see license.txt)                                         -
 *  -------------------------------------------------------------------------------------------------
 */

package org.objectify

/**
  * Syntacic sugar for simplifying Objectify definitions
  */
trait ObjectifySugar {
    def ~:[T <: AnyRef](implicit manifest: Manifest[T]): Class[T] = {
        manifest.runtimeClass.asInstanceOf[Class[T]]
    }

    def -:[T <: AnyRef](implicit manifest: Manifest[T]): Option[Class[T]] = {
        Some(manifest.runtimeClass.asInstanceOf[Class[T]])
    }
}
