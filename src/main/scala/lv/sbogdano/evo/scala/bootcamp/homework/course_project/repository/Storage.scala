package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError

trait Storage {

  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]]

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]]

}
