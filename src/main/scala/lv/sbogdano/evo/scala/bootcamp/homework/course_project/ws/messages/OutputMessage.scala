package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionGenericDerivation._

trait OutputMessage {
  def forUser(targetUser: String): Boolean
  def toString: String
}

case class SendToUser(userLogin: UserLogin, outputAction: OutputAction) extends OutputMessage {
  override def forUser(targetUser: String): Boolean = targetUser == userLogin
  override def toString: String = outputAction.asJson.noSpaces
}

