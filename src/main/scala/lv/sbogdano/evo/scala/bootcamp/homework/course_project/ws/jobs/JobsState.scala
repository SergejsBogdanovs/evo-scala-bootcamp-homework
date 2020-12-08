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

    case EnterJobScheduleInputAction() => ???
//      service.listJobs(msg.userLogin, All()) match {
//        case Some(_) =>
//          //(this, Seq(SendToWorker(worker, jobSchedule.toString())))
//          //addToWorkerJobs(worker, jobSchedule)
//          (this, sendToWorker(msg.userLogin))
//
//        case None =>
//          val (finalState, message) = addToWorkerJobs(msg.userLogin, emptyJobSchedule)
//          (finalState, Seq(WelcomeUser(msg.userLogin)) ++ message)
//      }

    case ListJobsInputAction(status) =>
      val jobs: Either[String, List[StationEntity]] = service.listJobs(msg.userLogin, status)
      jobs match {
          case Left(error)     => (this, Seq(SendToUser(msg.userLogin, error)))
          case Right(stations) => (this, Seq(SendToUser(msg.userLogin, stations.asJson.noSpaces)))
      }

    case AddJobInputAction(toUser, stationEntities) =>
      service.addJobsToUser(toUser, stationEntities) match {
        case Left(error) => (this, Seq(SendToUser(toUser, error)))
        case Right(jobs) => addToWorkerJobs(msg.userLogin, jobs)
      }

    case MarkJobAsCompletedInputAction(stationEntity) =>
      service.markJobAsCompleted(msg.userLogin, stationEntity) match {
        case Left(error) => (this, Seq(SendToUser(msg.userLogin, error)))
        case Right(jobs) => addToWorkerJobs(msg.userLogin, jobs)
      }

    case InvalidInputInputAction() =>
      (this, Seq(SendToUser(msg.userLogin, "Invalid input")))

    case DisconnectInputAction() => ???

  }

  private def addToWorkerJobs(userLogin: UserLogin, jobs: Map[UserLogin, JobSchedule]): (JobsState, Seq[OutputMessage]) = {
    val nextState = JobsState(StationService(CacheStorage(jobs)))
    (nextState, Seq(SendToUser(userLogin, jobs.toString()))) //TODO make jobs.asJson.noSpaces
  }

//  private def sendToWorker(userLogin: UserLogin): Seq[OutputMessage] =
//    jobs.get(userLogin)
//      .map { jobs: JobSchedule =>
//        jobs.get(Pending()) match {
//          case Some(jobs) => if (jobs.isEmpty) SendToUser(userLogin, "You have no jobs for today") else SendToUser(userLogin, s"Jobs for today: $jobs")
//          case None => SendToUser(userLogin, "You have no jobs for today")
//        }
//      }.toSeq
}

