package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{OutputActionError, UpdateJobError, UpdateJobResult, UserJobSchedule}

trait Storage {


  def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]]

  def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, UpdateStationSuccess]]

  def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]]

  def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DeleteStationSuccess]]



  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule]

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule]

  def updateJobStatus(userLogin: UserLogin, jobId: Int, status: Status): Either[OutputActionError, UserJobSchedule]

  def updateJobPriority(userLogin: UserLogin, jobId: Int, priority: Priority): Either[OutputActionError, UserJobSchedule]

  def addJobToSchedule(job: Job): Either[OutputActionError, UserJobSchedule]

  def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule]

  def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult]
}
