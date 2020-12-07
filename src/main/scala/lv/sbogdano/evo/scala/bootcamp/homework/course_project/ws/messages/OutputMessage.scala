package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.WorkerJobsState.Worker

trait OutputMessage {
  def forWorker(targetWorker: String): Boolean
  def toString: String
}

case class WelcomeWorker(worker: Worker) extends OutputMessage {
  override def forWorker(targetWorker: String): Boolean = targetWorker == worker
  override def toString: String = s"Welcome, ${worker.capitalize}! Today is another great day for work."
}

case class SendToWorker(worker: String, text: String) extends OutputMessage {
  override def forWorker(targetWorker: String): Boolean = targetWorker == worker
  override def toString: String = text
}

//case class SendToWorkers(workers: Set[String], text: String) extends OutputMessage {
//  override def forWorker(targetWorker: String): Boolean = workers.contains(targetWorker)
//  override def toString: String = text
//}

