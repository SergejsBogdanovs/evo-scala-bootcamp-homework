package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action.{OutputActionError, UpdateJobError, UpdateJobResult, UserJobSchedule}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

trait Storage {


  def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, DatabaseCreateStationSuccess]]

  def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, DatabaseUpdateStationSuccess]]

  def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]]

  def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DatabaseDeleteStationSuccess]]

  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule]

  def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult]
}
