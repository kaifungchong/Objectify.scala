package org.objectify.services

import javax.inject.Named

/**
  * Sample service
  */
class PicturesShowService(@Named("IdResolver") id: Int) extends Service[String] {
    def apply() = "show " + id
}
