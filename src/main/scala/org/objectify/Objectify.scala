package org.objectify

import org.objectify.services.Service
import org.objectify.responders.Responder
import org.objectify.policies.Policy

case class Objectify(defaults: Defaults = Defaults(), actions: Actions = Actions()) {

//    val reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.matygo.controllers")).setScanners(new ResourcesScanner()))
//    val controllers = reflections.getStore().getSubTypesOf("com.matygo.controllers.MatygoController")

    override def toString() = {
        "Objectify Configuration" + actions.toString
    }

}

object Test extends App {
	import Verb._
	
    val objectify = Objectify()

    objectify.defaults policy classOf[Policy]

    objectify.actions resource("pictures", index = Some(Action(GET, "index", policies = Some(List(classOf[Policy])), service = Some(classOf[Service]), responder = Some(classOf[Responder]))))

    println(objectify.toString)
}