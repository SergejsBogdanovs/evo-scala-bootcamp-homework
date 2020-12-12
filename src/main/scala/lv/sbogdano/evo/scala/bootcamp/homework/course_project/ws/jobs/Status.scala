package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait Status
case object Completed extends Status
case object Pending extends Status

object Status {
  private implicit val config: Configuration =
    Configuration.default.copy(transformConstructorNames = _.toLowerCase)

  implicit val statusCodec: Codec[Status] = deriveEnumerationCodec[Status]
}
