package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages

import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action.OutputAction
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action.OutputActionGenericDerivation._

case class OutputMessage(userLogin: UserLogin, outputAction: OutputAction) {
  def forUser(targetUser: String): Boolean = targetUser == userLogin
  override def toString: String = outputAction.asJson.noSpaces
}

