package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionGenericDerivation._

case class OutputMessage(userLogin: UserLogin, outputAction: OutputAction) {
  def forUser(targetUser: String): Boolean = targetUser == userLogin
  override def toString: String = outputAction.asJson.noSpaces
}

