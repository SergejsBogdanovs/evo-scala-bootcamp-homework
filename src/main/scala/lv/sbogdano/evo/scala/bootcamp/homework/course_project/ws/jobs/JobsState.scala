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
      addToUserJobs(msg.userLogin, Map(msg.userLogin -> emptyJobSchedule))

    case ListJobsInputAction(status) =>
      val jobs: Either[String, List[StationEntity]] = service.listJobs(msg.userLogin, status)
      jobs match {
          case Left(error)     => (this, Seq(SendToUser(msg.userLogin, error)))
          case Right(stations) => (this, Seq(SendToUser(msg.userLogin, stations.asJson.noSpaces)))
      }

    case AddJobsInputAction(toUser, stationEntities) =>
      service.addJobsToUser(toUser, stationEntities) match {
        case Left(error) => (this, Seq(SendToUser(toUser, error)))
        case Right(jobs) => addToUserJobs(msg.userLogin, jobs)
      }

    case MarkJobAsCompletedInputAction(stationEntity) =>
      service.markJobAsCompleted(msg.userLogin, stationEntity) match {
        case Left(error) => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(jobs) => addToUserJobs(msg.userLogin, jobs)
      }

    case InvalidInputInputAction() =>
      (this, Seq(SendToUser(msg.userLogin, "Invalid input")))

  }

  private def addToUserJobs(userLogin: UserLogin, jobs: Map[UserLogin, JobSchedule]): (JobsState, Seq[OutputMessage]) = {
    jobs.get(userLogin) match {
      case Some(jobSchedule) =>
        val nextState = JobsState(StationService(CacheStorage(jobs)))

        if (jobSchedule == emptyJobSchedule)
          (nextState, Seq(WelcomeUser(userLogin)))
        else
          (nextState, Seq(SendToUser(userLogin, jobs.toString()))) //TODO make jobs.asJson.noSpaces

      case None => (this, Seq(SendToUser(userLogin, "Can not find any jobs")))
    }
  }

  private def welcomeUser(userLogin: UserLogin, stations: List[StationEntity]): Seq[OutputMessage] =
    if (stations.isEmpty)
      Seq(SendToUser(userLogin, "You have no jobs for today"))
    else
      Seq(SendToUser(userLogin, s"Jobs for today: $stations"))
}

