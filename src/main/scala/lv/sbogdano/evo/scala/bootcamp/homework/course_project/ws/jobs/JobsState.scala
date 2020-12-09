package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import cats.effect.{ExitCode, IO}
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._
import io.circe.syntax._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin, emptyJobSchedule}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.InputAction._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.OutputAction.{ErrorOutputAction, ListJobsOutputAction, WelcomeOutputAction}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status.{All, Completed, Pending}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages._

object JobsState {
  // Default state
  def apply(): JobsState = JobsState(service)

  val service: StationService = StationService(CacheStorage())
  type JobSchedule = Map[Status, List[StationEntity]]
  type UserLogin = String

  val emptyJobSchedule: Map[Status, List[StationEntity]] = Map(Pending() -> List.empty)
}

case class JobsState(service: StationService) {

  def process(msg: InputMessage): (JobsState, Seq[OutputMessage]) = msg.action match {

    case EnterJobScheduleInputAction() =>
      updateState(
        msg.userLogin,
        Map(msg.userLogin -> emptyJobSchedule),
        WelcomeOutputAction(s"Welcome, ${msg.userLogin.capitalize}! Today is another great day for work.")
      )

    case ListJobsInputAction(status) =>
      val jobs: Either[ErrorOutputAction, ListJobsOutputAction] = service.listJobs(msg.userLogin, status)
      jobs match {
          case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))
          case Right(outputAction)     => (this, Seq(SendToUser(msg.userLogin, outputAction)))
      }

    case AddJobsInputAction(toUser, stationEntities) =>

      service.addJobsToUser(toUser, stationEntities) match {

        case Left(errorOutputAction) => (this, Seq(SendToUser(toUser, errorOutputAction)))

        case Right(outputAction)     =>
          updateState(
            msg.userLogin,
            outputAction.jobs,
            outputAction
          )
      }

    case MarkJobAsCompletedInputAction(stationEntity) =>

      service.markJobAsCompleted(msg.userLogin, stationEntity) match {

        case Left(errorOutputAction) => (this, Seq(SendToUser(msg.userLogin, errorOutputAction)))

        case Right(outputAction)     =>
          updateState(
            msg.userLogin,
            outputAction.jobs,
            outputAction
          )
      }

    case InvalidInputInputAction() =>
      (this, Seq(SendToUser(msg.userLogin, ErrorOutputAction("Invalid input"))))

  }

  private def updateState(userLogin: UserLogin, jobs: Map[UserLogin, JobSchedule], outputAction: OutputAction): (JobsState, Seq[OutputMessage]) = {

    val nextState = JobsState(StationService(CacheStorage(jobs)))
    (nextState, Seq(SendToUser(userLogin, outputAction)))

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
  }

//  private def welcomeUser(userLogin: UserLogin, stations: List[StationEntity]): Seq[OutputMessage] =
//    if (stations.isEmpty)
//      Seq(SendToUser(userLogin, "You have no jobs for today"))
//    else
//      Seq(SendToUser(userLogin, s"Jobs for today: $stations"))
}

