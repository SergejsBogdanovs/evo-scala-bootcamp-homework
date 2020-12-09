package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.effect.IO
import cats.implicits.{catsSyntaxEitherId, toBifunctorOps}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError.{DeleteStationError, FilterStationError, UpdateStationError}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.OutputAction.{AddJobsOutputAction, ErrorOutputAction, ListJobsOutputAction, MarkJobAsCompletedOutputAction}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{OutputAction, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status.{All, Completed, Pending}

import scala.util.Try


class CacheStorage(jobs: Map[UserLogin, JobSchedule], var stations: List[StationEntity]) extends Storage {

  type JobSchedule = Map[Status, List[StationEntity]]
  type UserLogin = String


  override def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = {
    stations = stations :+ stationEntity
    IO(stationEntity.asRight)
  }

  override def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = {

    stations.find(s => s.uniqueName == stationEntity.uniqueName) match {
      case Some(_) => {
        stations = stations map {
          s => if (s.uniqueName == stationEntity.uniqueName) stationEntity else s
        }
        IO(stationEntity.asRight)
      }
      case None => IO(UpdateStationError("Not found station to update").asLeft)
    }
  }

  override def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = {
    stations.filter(s => s.name == name) match {
      case x :: xs => IO((x :: xs).asRight)
      case Nil => IO(FilterStationError("Not found any station").asLeft)
    }
  }

  override def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = {

    stations.find(s => s.uniqueName == uniqueName) match {
      case Some(value) => {
        stations = stations.filter(s => s.uniqueName != value.uniqueName)
        IO(value.uniqueName.asRight)
      }
      case None => IO(DeleteStationError("Not found station to delete").asLeft)
    }
  }

  override def getJobs(userLogin: UserLogin, status: Status): Either[ErrorOutputAction, ListJobsOutputAction] =
    jobs.get(userLogin) match {
      case Some(jobSchedule) =>

        val completedStations: List[StationEntity] = jobSchedule.getOrElse(Completed(), List.empty)
        val pendingStations: List[StationEntity] = jobSchedule.getOrElse(Pending(), List.empty)
        val allStations = completedStations ++ pendingStations

        val stations = status match {
          case Completed() => completedStations
          case Pending()   => pendingStations
          case All()       => allStations
        }

        if (stations.isEmpty) {
          ErrorOutputAction("Can not find any jobs").asLeft
        } else {
          ListJobsOutputAction(stations).asRight
        }

      case None =>
        ErrorOutputAction(s"Can not find user: $userLogin").asLeft
    }

  override def addJobsToUser(toUser: UserLogin, stationEntities: List[StationEntity]): Either[ErrorOutputAction, AddJobsOutputAction] =
    jobs.get(toUser) match {
      case Some(jobSchedule) =>

        val newJobSchedule = jobSchedule.flatMap { case (status, stations) => Map(status -> (stations ++ stationEntities)) }

        Try(jobs + (toUser -> newJobSchedule)).toEither match {
          case Left(_) => ErrorOutputAction("Error adding new jobs to job schedule").asLeft
          case Right(jobs) => AddJobsOutputAction(jobs).asRight
        }

      case None =>

        val js: JobSchedule = Map(Pending() -> stationEntities)

        Try(jobs + (toUser -> js)).toEither match {
          case Left(_) => ErrorOutputAction("Error adding new jobs to job schedule").asLeft
          case Right(jobs) => AddJobsOutputAction(jobs).asRight
        }
    }

  override def markJobAsCompleted(userLogin: UserLogin, stationEntity: StationEntity): Either[ErrorOutputAction, MarkJobAsCompletedOutputAction] =
    jobs.get(userLogin) match {

      // Getting worker job schedule
      case Some(jobSchedule) =>

        // Getting pending jobs
        jobSchedule.get(Pending()) match {

          case Some(pendingJobs) =>

            val newPendingJobs = pendingJobs.filter(job => job.uniqueName != stationEntity.uniqueName)
            val completedJobs = pendingJobs.filter(job => job.uniqueName == stationEntity.uniqueName)
            val newJobSchedule: Map[Status, List[StationEntity]] = jobSchedule + (Pending() -> newPendingJobs, Completed() -> completedJobs)
            MarkJobAsCompletedOutputAction(Map(userLogin -> newJobSchedule)).asRight

          case None => ErrorOutputAction("Can not find any pending jobs").asLeft
        }

      case None => ErrorOutputAction(s"Can not find user: $userLogin").asLeft

    }
}

object CacheStorage {
  def apply(
             jobs: Map[UserLogin, JobSchedule] = Map.empty,
             stations: List[StationEntity] = List.empty
           ) = new CacheStorage(jobs, stations)
}