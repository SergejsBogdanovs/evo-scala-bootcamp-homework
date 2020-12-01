package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError

class StationService(storage: Storage) {

  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.createStation(stationEntity)

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.updateStation(stationEntity)

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = storage.filterStations(name)

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = storage.deleteStation(uniqueName)
}

object StationService {
  def apply(storage: Storage) = new StationService(storage)
}
