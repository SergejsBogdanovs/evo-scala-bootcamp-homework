package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{OutputMessage, Status}

class StationService(storage: Storage) {

  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.createStation(stationEntity)

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.updateStation(stationEntity)

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = storage.filterStations(name)

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = storage.deleteStation(uniqueName)


  // JobsSchedule
  def listJobs(userLogin: UserLogin, status: Status): Either[String, List[StationEntity]] = storage.getJobs(userLogin, status)

  def markJobAsCompleted(userLogin: UserLogin, stationEntity: StationEntity): Either[String, Map[UserLogin, JobSchedule]] = storage.markJobAsCompleted(userLogin, stationEntity)

//  def updateJobScheduleState(userLogin: UserLogin, jobSchedule: JobSchedule): Either[String, Map[UserLogin, JobSchedule]] =
//    storage.updateJobScheduleState(userLogin, jobSchedule)

  def addJobsToUser(toUser: UserLogin, stationEntities: List[StationEntity]): Either[String, Map[UserLogin, JobSchedule]] = storage.addJobsToUser(toUser, stationEntities)
}

object StationService {
  def apply(storage: Storage) = new StationService(storage)
}
