package org.objectify

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions()) {
  override def toString() = {
    "Objectify Configuration" + actions.toString
  }
}
