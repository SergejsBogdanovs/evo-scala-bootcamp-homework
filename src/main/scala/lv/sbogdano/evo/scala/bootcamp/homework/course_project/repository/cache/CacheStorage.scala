package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository.RepositoryOps.{CreateStationError, CreateStationSuccess, DeleteStationError, DeleteStationSuccess, FilterStationError, FilterStationSuccess, UpdateStationError, UpdateStationSuccess}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, Priority, Rejected, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._

import scala.util.Try

class CacheStorage(jobsSchedule: JobSchedule, var stations: List[StationEntity]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]] = IO {
    Try(stations :+ stationEntity).toEither match {
      case Left(_)      => CreateStationError("Error during insertion into Database").asLeft
      case Right(value) =>
        stations = value
        CreateStationSuccess(stationEntity).asRight
    }
  }

  override def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, UpdateStationSuccess]] = IO {
    val i = stations.indexWhere(_.uniqueName == stationEntity.uniqueName)
    if (i == -1)
      UpdateStationError("Not found station to update").asLeft
    else {
      stations.updated(i, stationEntity)
      UpdateStationSuccess(stationEntity).asRight
    }
  }

  override def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]] = IO {
    val s = stations.filter(_.name == name)
    if (s.isEmpty)
      FilterStationError("Not found any station").asLeft
    else
      FilterStationSuccess(s).asRight
  }

  override def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DeleteStationSuccess]] = IO {
    stations.find(_.uniqueName == uniqueName) match {
      case Some(value) =>
        stations = stations.filter(_.uniqueName != value.uniqueName)
        DeleteStationSuccess(uniqueName).asRight
      case None => DeleteStationError("Not found station to delete").asLeft
    }
  }

  // SCHEDULE

  override def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(job => job.userLogin == userLogin && job.status != Rejected)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs.sorted).asRight
    }
  }

  override def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(job => job.userLogin == userLogin && job.status == status)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs.sorted).asRight
    }
  }

  override def addJobToSchedule(jobToAdd: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(userJob => userJob.userLogin == jobToAdd.userLogin && userJob.station == jobToAdd.station) match {
      case Some(_) => AddJobError("Already exist").asLeft
      case None    => UserJobSchedule((jobsSchedule :+ jobToAdd).sorted).asRight
    }
  }

  override def updateJobStatus(userLogin: UserLogin, jobId: Int, newStatus: Status): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(status = newStatus) else job)
        UserJobSchedule(updated.sorted).asRight

      case None    => UpdateJobError("Couldn't find job to update status by provided user and/or job id").asLeft
    }
  }

  override def updateJobPriority(userLogin: UserLogin, jobId: Int, newPriority: Priority): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(priority = newPriority) else job)
        UserJobSchedule(updated.sorted).asRight

      case None    => UpdateJobError("Couldn't find job to update priority by provided user and/or job id").asLeft
    }
  }

  override def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(_ == job) match {
      case Some(_) => UserJobSchedule(jobsSchedule.filter(_ != job).sorted).asRight
      case None    => DeleteJobError("Couldn't find job to delete").asLeft
    }
  }

  def getJobSchedule: JobSchedule = jobsSchedule

  override def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult] = ???
}

object CacheStorage {

  def apply(
             jobsSchedule: JobSchedule = List.empty,
             stations: List[StationEntity] = List.empty
           ) = new CacheStorage(jobsSchedule, stations)
}