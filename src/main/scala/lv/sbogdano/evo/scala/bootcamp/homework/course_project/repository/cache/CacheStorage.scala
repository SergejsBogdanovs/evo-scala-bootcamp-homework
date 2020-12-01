package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError.{DeleteStationError, FilterStationError, UpdateStationError}


class CacheStorage extends Storage {
  // TODO complete
  var stations: List[StationEntity] = List.empty[StationEntity]

  override def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = {
    stations = stations :+ stationEntity
    IO(stationEntity.asRight)
  }

  override def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = {

    stations.find(s => s.uniqueName == stationEntity.uniqueName) match {
      case Some(_) => {
        stations = stations map {
          s => if (s.uniqueName == stationEntity.uniqueName) stationEntity else s
        }
        IO(stationEntity.asRight)
      }
      case None => IO(UpdateStationError("Not found station to update").asLeft)
    }
  }

  override def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = {
    stations.filter(s => s.name == name) match {
      case x :: xs => IO((x :: xs).asRight)
      case Nil     => IO(FilterStationError("Not found any station").asLeft)
    }
  }

  override def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = {

    stations.find(s => s.uniqueName == uniqueName) match {
      case Some(value) => {
        stations = stations.filter(s => s.uniqueName != value.uniqueName)
        IO(value.uniqueName.asRight)
      }
      case None => IO(DeleteStationError("Not found station to delete").asLeft)
    }
  }


}

object CacheStorage {
  def apply() = new CacheStorage()
}