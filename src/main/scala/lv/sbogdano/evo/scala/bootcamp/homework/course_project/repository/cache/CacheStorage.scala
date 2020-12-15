package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action._

class CacheStorage(jobsSchedule: JobSchedule, var stations: List[StationEntity]) extends Storage {

  // TODO refactor
  override def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]] = IO {
    stations = stations :+ stationEntity
    CreateStationSuccess(stationEntity).asRight
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
    val userJobs: JobSchedule = jobsSchedule.filter(_.userLogin == userLogin)
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
}

object CacheStorage {

  def apply(
             jobsSchedule: JobSchedule = List.empty,
             stations: List[StationEntity] = List.empty
           ) = new CacheStorage(jobsSchedule, stations)
}