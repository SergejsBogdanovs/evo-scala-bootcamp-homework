package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action._

object JobsState {
  def apply(): JobsState = JobsState(CacheStorage())
  type JobSchedule = List[Job]
  type UserLogin = String
}

case class JobsState(storage: Storage) {

  def process(msg: InputMessage): (JobsState, Seq[OutputMessage]) = msg.action match {

    case EnterJobSchedule =>
      storage.findJobsByUser(msg.userLogin) match {
        case Left(_) =>
          updateState(
            userLogin = msg.userLogin,
            outputAction = WelcomeUser(s"Welcome, ${msg.userLogin.capitalize}! Today is another great day for work.")
          )
        case Right(value) =>(this, Seq(OutputMessage(msg.userLogin, WelcomeUser(s"Welcome ${msg.userLogin}, jobs for today ${value.jobSchedule}"))))
      }

    case FindJobsByUser =>
      storage.findJobsByUser(msg.userLogin) match {
        case Left(errorOutputAction) => (this, Seq(OutputMessage(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(OutputMessage(msg.userLogin, listUserJobs)))
      }

    case FindJobsByUserAndStatus(status) =>
      storage.findJobsByUserAndStatus(msg.userLogin, status) match {
        case Left(errorOutputAction) => (this, Seq(OutputMessage(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(OutputMessage(msg.userLogin, listUserJobs)))
      }

    case AddJobToSchedule(job) =>
      storage.addJobToSchedule(job) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobStatus(jobId, status) =>
      storage.updateJobStatus(msg.userLogin, jobId, status) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobPriority(jobId, priority) =>
      storage.updateJobPriority(msg.userLogin, jobId, priority) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case DeleteJobFromSchedule(job) =>
      storage.deleteJobFromSchedule(job) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>           updateState(
          userLogin = msg.userLogin,
          jobSchedule = outputAction.jobSchedule,
          outputAction = outputAction
        )
      }

    case InvalidInput =>
      (this, Seq(OutputMessage(msg.userLogin, InvalidInputError("Invalid input"))))

  }

  private def updateState(userLogin: UserLogin, jobSchedule: JobSchedule = List.empty, outputAction: OutputAction): (JobsState, Seq[OutputMessage]) = {
    val nextState = JobsState(CacheStorage(jobSchedule))
    (nextState, Seq(OutputMessage(userLogin, outputAction)))
  }
}

