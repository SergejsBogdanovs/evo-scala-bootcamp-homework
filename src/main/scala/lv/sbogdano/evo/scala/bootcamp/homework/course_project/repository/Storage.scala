package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status

trait Storage {


  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]]

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]]


  def getJobs(userLogin: UserLogin, status: Status): Either[String, List[StationEntity]]

  def markJobAsCompleted(userLogin: UserLogin, stationEntity: StationEntity): Either[String, Map[UserLogin, JobSchedule]]

//  def updateJobScheduleState(userLogin: UserLogin, jobSchedule: JobSchedule): Either[String, Map[UserLogin, JobSchedule]]

  def addJobsToUser(toUser: UserLogin, stationEntities: List[StationEntity]): Either[String, Map[UserLogin, JobSchedule]]
}
