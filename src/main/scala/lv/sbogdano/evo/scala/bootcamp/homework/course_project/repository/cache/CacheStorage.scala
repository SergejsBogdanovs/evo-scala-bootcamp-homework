package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError.{DeleteStationError, FilterStationError, UpdateStationError}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.UserJobSchedule
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError.{AddJobError, DeleteJobError, FindJobsError, UpdateJobError}

import scala.util.{Failure, Success, Try}

// TODO maybe ExpiringCache jobSchedule?
class CacheStorage(jobsSchedule: JobSchedule, var stations: List[StationEntity]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = IO {
    stations = stations :+ stationEntity
    stationEntity.asRight
  }

  override def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = IO {
    val i = stations.indexWhere(_.uniqueName == stationEntity.uniqueName)
    if (i == -1)
      UpdateStationError("Not found station to update").asLeft
    else {
      stations.updated(i, stationEntity)
      stationEntity.asRight
    }
  }

  override def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = IO {
    val s = stations.filter(_.name == name)
    if (s.isEmpty) FilterStationError("Not found any station").asLeft else s.asRight
  }

  override def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = IO {
    stations.find(_.uniqueName == uniqueName) match {
      case Some(value) =>
        stations = stations.filter(_.uniqueName != value.uniqueName)
        uniqueName.asRight
      case None => DeleteStationError("Not found station to delete").asLeft
    }
  }

  // SCHEDULE

  override def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(_.userLogin == userLogin)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs).asRight
    }
  }

  override def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(job => job.userLogin == userLogin && job.status == status)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs).asRight
    }
  }

  override def addJobToSchedule(jobToAdd: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(userJob => userJob.userLogin == jobToAdd.userLogin && userJob.station == jobToAdd.station) match {
      case Some(_) => AddJobError("Already exist").asLeft
      case None    => UserJobSchedule(jobsSchedule :+ jobToAdd).asRight // TODO sort by priority
    }
  }

  override def updateJobStatus(userLogin: UserLogin, jobId: Long, newStatus: Status): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(status = newStatus) else job)
        UserJobSchedule(updated).asRight

      case None    => UpdateJobError("Couldn't find job to update status by provided user and/or job id").asLeft
    }
  }

  override def updateJobPriority(userLogin: UserLogin, jobId: Long, newPriority: Priority): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(priority = newPriority) else job)
        UserJobSchedule(updated).asRight

      case None    => UpdateJobError("Couldn't find job to update priority by provided user and/or job id").asLeft
    }
  }

  override def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(_ == job) match {
      case Some(_) => UserJobSchedule(jobsSchedule.filter(_ != job)).asRight
      case None    => DeleteJobError("Couldn't find job to delete").asLeft
    }
  }

  // SCHEDULE
//  override def getJobs(userLogin: UserLogin, status: Status): Either[ErrorOutput, ListJobsOutput] =
//    jobs.get(userLogin) match {
//      case Some(jobSchedule) =>
//
//        val completedStations: List[StationEntity] = jobSchedule.getOrElse(Completed, List.empty)
//        val pendingStations: List[StationEntity] = jobSchedule.getOrElse(Pending, List.empty)
//        val allStations = completedStations ++ pendingStations
//
//        val stations = status match {
//          case Completed => completedStations
//          case Pending   => pendingStations
//          case All       => allStations
//        }
//
//        if (stations.isEmpty) {
//          ErrorOutput("Can not find any jobs").asLeft
//        } else {
//          ListJobsOutput(stations).asRight
//        }
//
//      case None =>
//        ErrorOutput(s"Can not find user: $userLogin").asLeft
//    }
//
//  override def addJobsToUser(toUser: UserLogin, stationEntities: List[StationEntity]): Either[ErrorOutput, AddJobsOutput] =
//    jobs.get(toUser) match {
//      case Some(jobSchedule) =>
//
//        val newJobSchedule = jobSchedule.flatMap { case (status, stations) => Map(status -> (stations ++ stationEntities)) }
//
//        Try(jobs + (toUser -> newJobSchedule)).toEither match {
//          case Left(_) => ErrorOutput("Error adding new jobs to job schedule").asLeft
//          case Right(jobs) => AddJobsOutput(jobs).asRight
//        }
//
//      case None =>
//
//        val js: JobSchedule = Map(Pending -> stationEntities)
//
//        Try(jobs + (toUser -> js)).toEither match {
//          case Left(_) => ErrorOutput("Error adding new jobs to job schedule").asLeft
//          case Right(jobs) => AddJobsOutput(jobs).asRight
//        }
//    }
//
//  override def markJobAsCompleted(userLogin: UserLogin, stationEntity: StationEntity): Either[ErrorOutput, MarkJobAsCompletedOutput] =
//    jobs.get(userLogin) match {
//
//      // Getting worker job schedule
//      case Some(jobSchedule) =>
//
//        // Getting pending jobs
//        jobSchedule.get(Pending) match {
//
//          case Some(pendingJobs) =>
//
//            val newPendingJobs = pendingJobs.filter(job => job.uniqueName != stationEntity.uniqueName)
//            val completedJobs = pendingJobs.filter(job => job.uniqueName == stationEntity.uniqueName)
//            val newJobSchedule: Map[Status, List[StationEntity]] = jobSchedule + (Pending -> newPendingJobs, Completed -> completedJobs)
//            MarkJobAsCompletedOutput(Map(userLogin -> newJobSchedule)).asRight
//
//          case None => ErrorOutput("Can not find any pending jobs").asLeft
//        }
//
//      case None => ErrorOutput(s"Can not find user: $userLogin").asLeft
//
//    }
}

object CacheStorage {

  def apply(
             jobsSchedule: JobSchedule = List.empty,
             stations: List[StationEntity] = List.empty
           ) = new CacheStorage(jobsSchedule, stations)
}