package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import doobie.implicits._
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError._


class DatabaseStorage(transactor: Transactor[IO]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError,StationEntity]] = {
    for {
      insert <- StationQuery.insert(stationEntity).run.transact(transactor)
      result <- IO {
        if (insert == 1) stationEntity.asRight else CreateStationError("Error during insertion into Database").asLeft
      }
    } yield result
  }

  override def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = {
    for {
      update <- StationQuery.update(stationEntity).run.transact(transactor)
      result <- IO {
        if (update == 1) stationEntity.asRight else UpdateStationError("Error during update").asLeft
      }
    } yield result
  }

  override def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = {
    StationQuery.searchWithName(name).to[List].transact(transactor).attempt.map {
      case Left(throwable)  => FilterStationError(s"Error during station filtration: ${throwable.getMessage}").asLeft
      case Right(value)     => value.asRight
    }
  }

  override def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = {
    for {
      delete <- StationQuery.delete(uniqueName).run.transact(transactor)
      result <- IO {
        if (delete == 1) uniqueName.asRight else DeleteStationError("Error during delete").asLeft
      }
    } yield result
  }
}