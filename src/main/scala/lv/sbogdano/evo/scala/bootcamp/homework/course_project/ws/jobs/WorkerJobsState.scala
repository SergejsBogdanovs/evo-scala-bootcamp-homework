package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.Status.{Completed, Pending}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.WorkerJobsState.{JobSchedule, Worker}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.InputMessage._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage, SendToWorker, WelcomeWorker}


sealed trait Status

object Status {
  object Completed extends Status
  object Pending extends Status
}

object WorkerJobsState {
  // Default state
  def apply(): WorkerJobsState = WorkerJobsState(Map.empty)
  type Worker = String
  type JobSchedule = Map[Status, List[StationEntity]]
}

case class WorkerJobsState(workerJobs: Map[Worker, JobSchedule]) {

  def process(msg: InputMessage): (WorkerJobsState, Seq[OutputMessage]) = msg match {

    case EnterJobSchedule(worker, emptyJobSchedule) =>
      workerJobs.get(worker) match {
        case Some(jobSchedule) =>
          //(this, Seq(SendToWorker(worker, jobSchedule.toString())))
          //addToWorkerJobs(worker, jobSchedule)
          (this, sendToWorker(worker))

        case None => {
          val (finalState, message) = addToWorkerJobs(worker, emptyJobSchedule)
          (finalState, Seq(WelcomeWorker(worker)) ++ message)
        }
      }

    case Help(worker) => (this, Seq(SendToWorker(worker, InputMessage.helpText)))

    case ListJobsAll(worker) =>
      workerJobs.get(worker) match {
        case Some(jobSchedule) =>

          val completedJobs = jobSchedule.getOrElse(Completed, List.empty)
          val pendingJobs = jobSchedule.getOrElse(Pending, List.empty)
          val allJobs = completedJobs ++ pendingJobs

          if (allJobs.isEmpty)
            (this, Seq(SendToWorker(worker, "Can not find any jobs")))
          else
            (this, Seq(SendToWorker(worker, s"All jobs: $allJobs")))

        case None =>
          (this, Seq(SendToWorker(worker, s"Can not find worker $worker")))
      }

    case ListJobsCompleted(worker) =>
      workerJobs.get(worker) match {
        case Some(jobSchedule) =>
          jobSchedule.get(Completed) match {
            case Some(jobs) => (this, Seq(SendToWorker(worker, s"Completed jobs: $jobs")))

            case None => (this, Seq(SendToWorker(worker, "Can not find any completed jobs")))
          }

        case None =>
          (this, Seq(SendToWorker(worker, s"Can not find worker $worker")))
      }

    case ListJobsPending(worker) =>
      workerJobs.get(worker) match {
        case Some(jobSchedule) =>
          jobSchedule.get(Pending) match {
            case Some(jobs) => (this, Seq(SendToWorker(worker, s"Pending jobs: $jobs")))

            case None => (this, Seq(SendToWorker(worker, "Can not find any pending jobs")))
          }

        case None =>
          (this, Seq(SendToWorker(worker, s"Can not find worker $worker")))
      }


    case MarkJobAsCompleted(worker, uniqueName) =>
      println("Hello")
      workerJobs.get(worker) match {

          // Getting worker job schedule
        case Some(jobSchedule) =>

          // Getting pending jobs
          jobSchedule.get(Pending) match {

          case Some(pendingJobs) =>

            val newPendingJobs = pendingJobs.filter(job => job.uniqueName != uniqueName)
            val completedJobs = pendingJobs.filter(job => job.uniqueName == uniqueName)
            val newJobSchedule: Map[Status, List[StationEntity]] = jobSchedule + (Pending -> newPendingJobs)
            val updated: Map[Status, List[StationEntity]] = newJobSchedule + (Completed -> completedJobs)
            addToWorkerJobs(worker, updated)

          case None => (this, Seq(SendToWorker(worker, "Can not find any pending jobs")))
        }

        case None => (this, Seq(SendToWorker(worker, s"Can not find worker $worker")))

      }

    case InvalidInput(worker, text) =>
      (this, Seq(SendToWorker(worker, s"Invalid input: $text")))

    case Disconnect(worker) => ???

  }

  private def addToWorkerJobs(worker: Worker, jobSchedule: JobSchedule): (WorkerJobsState, Seq[OutputMessage]) = {
    val nextState = WorkerJobsState(workerJobs + (worker -> jobSchedule))
    //(nextState, Seq(SendToWorker(worker, jobSchedule.toString())))
    (nextState, sendToWorker(worker))
  }

  private def sendToWorker(worker: Worker): Seq[OutputMessage] =
    workerJobs.get(worker)
      .map { jobs: JobSchedule =>
        jobs.get(Pending) match {
          case Some(jobs) => if (jobs.isEmpty) SendToWorker(worker, "You have no jobs for today") else SendToWorker(worker, s"Jobs for today: $jobs")
          case None => SendToWorker(worker, "You have no jobs for today")
        }
      }.toSeq
}

