package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.OutputAction.{AddJobsOutputAction, ErrorOutputAction, ListJobsOutputAction, MarkJobAsCompletedOutputAction}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status

class StationService(storage: Storage) {

  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.createStation(stationEntity)

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.updateStation(stationEntity)

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = storage.filterStations(name)

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = storage.deleteStation(uniqueName)


  // JobsSchedule
  def listJobs(userLogin: UserLogin, status: Status): Either[ErrorOutputAction, ListJobsOutputAction] = storage.getJobs(userLogin, status)

  def markJobAsCompleted(userLogin: UserLogin, stationEntity: StationEntity): Either[ErrorOutputAction, MarkJobAsCompletedOutputAction] = storage.markJobAsCompleted(userLogin, stationEntity)

  def addJobsToUser(toUser: UserLogin, stationEntities: List[StationEntity]): Either[ErrorOutputAction, AddJobsOutputAction] = storage.addJobsToUser(toUser, stationEntities)
}

object StationService {
  def apply(storage: Storage) = new StationService(storage)
}
