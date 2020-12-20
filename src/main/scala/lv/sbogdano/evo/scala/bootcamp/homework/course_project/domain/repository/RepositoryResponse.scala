package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

sealed trait RepositoryResponse

sealed trait RepositoryError extends Throwable with RepositoryResponse

case class CacheCreateStationSuccess(stationEntities: List[StationEntity]) extends RepositoryResponse

case class DatabaseCreateStationSuccess(stationEntity: StationEntity) extends RepositoryResponse

case class CacheUpdateStationSuccess(stationEntities: List[StationEntity]) extends RepositoryResponse

case class DatabaseUpdateStationSuccess(stationEntity: StationEntity) extends RepositoryResponse

case class FilterStationSuccess(stationEntities: List[StationEntity]) extends RepositoryResponse

case class CacheDeleteStationSuccess(stationEntities: List[StationEntity]) extends RepositoryResponse

case class DatabaseDeleteStationSuccess(uniqueName: String) extends RepositoryResponse


case class CreateStationError(errorMessage: String) extends RepositoryError

case class UpdateStationError(errorMessage: String) extends RepositoryError

case class FilterStationError(errorMessage: String) extends RepositoryError

case class DeleteStationError(errorMessage: String) extends RepositoryError
