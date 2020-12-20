package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, Priority, Rejected, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

class CacheStorage(jobsSchedule: JobSchedule, stations: List[StationEntity]) {

  def createStation(stationEntity: StationEntity): Either[CreateStationError, CacheCreateStationSuccess] =
    stations.find(station => station.uniqueName == stationEntity.uniqueName) match {
      case Some(_) => CreateStationError("Already exist").asLeft
      case None    => CacheCreateStationSuccess(stations :+ stationEntity).asRight
    }

  def updateStation(stationEntity: StationEntity): Either[UpdateStationError, CacheUpdateStationSuccess] = {
    val i = stations.indexWhere(_.uniqueName == stationEntity.uniqueName)
    if (i == -1)
      UpdateStationError("Not found station to update").asLeft
    else {
      val updated = stations.updated(i, stationEntity)
      CacheUpdateStationSuccess(updated).asRight
    }
  }

  def filterStations(name: String): Either[FilterStationError, FilterStationSuccess] = {
    val s = stations.filter(_.name == name)
    if (s.isEmpty)
      FilterStationError("Not found any station").asLeft
    else
      FilterStationSuccess(s).asRight
  }

  def deleteStation(uniqueName: String): Either[DeleteStationError, CacheDeleteStationSuccess] = {
    stations.find(_.uniqueName == uniqueName) match {
      case Some(value) =>
        val filtered = stations.filter(_.uniqueName != value.uniqueName)
        CacheDeleteStationSuccess(filtered).asRight
      case None => DeleteStationError("Not found station to delete").asLeft
    }
  }

  // SCHEDULE

  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(job => job.userLogin == userLogin && job.status != Rejected)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs.sorted).asRight
    }
  }

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = {
    val userJobs: JobSchedule = jobsSchedule.filter(job => job.userLogin == userLogin && job.status == status)
    if (userJobs.isEmpty) {
      FindJobsError("Can not find any jobs").asLeft
    } else {
      UserJobSchedule(userJobs.sorted).asRight
    }
  }

  def addJobToSchedule(jobToAdd: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(userJob => userJob.userLogin == jobToAdd.userLogin && userJob.station == jobToAdd.station) match {
      case Some(_) => AddJobError("Already exist").asLeft
      case None    => UserJobSchedule((jobsSchedule :+ jobToAdd).sorted).asRight
    }
  }

  def updateJobStatus(userLogin: UserLogin, jobId: Int, newStatus: Status): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(status = newStatus) else job)
        UserJobSchedule(updated.sorted).asRight

      case None    => UpdateJobError("Couldn't find job to update status by provided user and/or job id").asLeft
    }
  }

  def updateJobPriority(userLogin: UserLogin, jobId: Int, newPriority: Priority): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(job => job.userLogin == userLogin && job.id == jobId) match {
      case Some(_) =>
        val updated = jobsSchedule.map(job => if (job.userLogin == userLogin && job.id == jobId) job.copy(priority = newPriority) else job)
        UserJobSchedule(updated.sorted).asRight

      case None    => UpdateJobError("Couldn't find job to update priority by provided user and/or job id").asLeft
    }
  }

  def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = {
    jobsSchedule.find(_ == job) match {
      case Some(_) => UserJobSchedule(jobsSchedule.filter(_ != job).sorted).asRight
      case None    => DeleteJobError("Couldn't find job to delete").asLeft
    }
  }

  def getJobSchedule: JobSchedule = jobsSchedule

}

object CacheStorage {

  def apply(
             jobsSchedule: JobSchedule = List.empty,
             stations: List[StationEntity] = List.empty
           ) = new CacheStorage(jobsSchedule, stations)
}