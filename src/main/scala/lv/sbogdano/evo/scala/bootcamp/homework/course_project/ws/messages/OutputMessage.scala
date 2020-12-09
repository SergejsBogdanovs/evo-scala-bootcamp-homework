package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin

trait OutputMessage {
  def forUser(targetUser: String): Boolean
  def toString: String
}

//case class WelcomeUser(userLogin: UserLogin) extends OutputMessage {
//  override def forUser(targetUser: String): Boolean = targetUser == userLogin
//  override def toString: String = s"Welcome, ${userLogin.capitalize}! Today is another great day for work."
//}

case class SendToUser(userLogin: UserLogin, outputAction: OutputAction) extends OutputMessage {
  override def forUser(targetUser: String): Boolean = targetUser == userLogin
  override def toString: String = outputAction.toString
}

//case class SendToWorkers(workers: Set[String], text: String) extends OutputMessage {
//  override def forWorker(targetWorker: String): Boolean = workers.contains(targetWorker)
//  override def toString: String = text
//}

