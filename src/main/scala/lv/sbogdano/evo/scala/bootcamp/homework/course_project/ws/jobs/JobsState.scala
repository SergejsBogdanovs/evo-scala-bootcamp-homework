package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.InputAction.{EnterJobSchedule, _}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.WelcomeUser
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError.InvalidInputError

object JobsState {
  // Default state
  def apply(): JobsState = JobsState(service)

  val service: StationService = StationService(CacheStorage())

  type JobSchedule = List[Job]
  type UserLogin = String
}

case class JobsState(service: StationService) {

  def process(msg: InputMessage): (JobsState, Seq[OutputMessage]) = msg.action match {

    case EnterJobSchedule =>
      updateState(
        userLogin = msg.userLogin,
        outputAction = WelcomeUser(s"Welcome, ${msg.userLogin.capitalize}! Today is another great day for work.")
      )

    case FindJobsByUser =>
      service.findJobsByUser(msg.userLogin) match {
        case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(SendToUser(msg.userLogin, listUserJobs)))
      }

    case FindJobsByUserAndStatus(status) =>
      service.findJobsByUserAndStatus(msg.userLogin, status) match {
        case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(SendToUser(msg.userLogin, listUserJobs)))
      }

    case AddJobToSchedule(job) =>
      service.addJobToSchedule(job) match {
        case Left(error)         => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobStatus(jobId, status) =>
      service.updateJobStatus(msg.userLogin, jobId, status) match {
        case Left(error)         => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobPriority(jobId, priority) =>
      service.updateJobPriority(msg.userLogin, jobId, priority) match {
        case Left(error)         => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case DeleteJobFromSchedule(job) =>
      service.deleteJobFroSchedule(job) match {
        case Left(error)         => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(outputAction) =>           updateState(
          userLogin = msg.userLogin,
          jobSchedule = outputAction.jobSchedule,
          outputAction = outputAction
        )
      }

//    case ListJobsInput(status) =>
//      val jobs: Either[ErrorOutput, ListJobsOutput] = service.listJobs(msg.userLogin, status)
//      jobs match {
//        case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))
//        case Right(outputAction) => (this, Seq(SendToUser(msg.userLogin, outputAction)))
//      }
//
//    case AddJobsInput(toUser, stationEntities) =>
//
//      service.addJobsToUser(toUser, stationEntities) match {
//
//        case Left(errorOutputAction) => (this, Seq(SendToUser(toUser, errorOutputAction)))
//
//        case Right(outputAction) =>
//          updateState(
//            msg.userLogin,
//            outputAction.jobs,
//            outputAction
//          )
//      }
//
//    case MarkJobAsCompletedInput(stationEntity) =>
//
//      service.markJobAsCompleted(msg.userLogin, stationEntity) match {
//
//        case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))
//
//        case Right(outputAction) =>
//          updateState(
//            msg.userLogin,
//            outputAction.jobs,
//            outputAction
//          )
//      }

    case InvalidInput =>
      (this, Seq(SendToUser(msg.userLogin, InvalidInputError("Invalid input"))))

  }


  private def updateState(userLogin: UserLogin, jobSchedule: JobSchedule = List.empty, outputAction: OutputAction): (JobsState, Seq[OutputMessage]) = {
    val nextState = JobsState(StationService(CacheStorage(jobSchedule)))
    (nextState, Seq(SendToUser(userLogin, outputAction)))
  }

//  private def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): (JobsState, Seq[OutputMessage]) = {
//    service.findJobsByUserAndStatus(userLogin, status) match {
//      case Left(errorOutputAction) => (this, Seq(SendToUser(userLogin, errorOutputAction)))
//      case Right(listUserJobs)     => (this, Seq(SendToUser(userLogin, listUserJobs)))
//    }
//  }

  //    jobs.get(userLogin) match {
  //      case Some(jobSchedule) =>
  //        val nextState = JobsState(StationService(CacheStorage(jobs)))
  //        (nextState, Seq(SendToUser(userLogin, outputAction)))
  //
  //
  //      //        if (jobSchedule == emptyJobSchedule)
  ////          (nextState, Seq(SendToUser(userLogin, WelcomeOutputAction())))
  ////        else
  ////          (nextState, Seq(SendToUser(userLogin, outputAction)))
  //
  //      case None => (this, Seq(SendToUser(userLogin, ErrorOutputAction("Can not find any jobs"))))
  //    }

  //  private def welcomeUser(userLogin: UserLogin, stations: List[StationEntity]): Seq[OutputMessage] =
  //    if (stations.isEmpty)
  //      Seq(SendToUser(userLogin, "You have no jobs for today"))
  //    else
  //      Seq(SendToUser(userLogin, s"Jobs for today: $stations"))
}

