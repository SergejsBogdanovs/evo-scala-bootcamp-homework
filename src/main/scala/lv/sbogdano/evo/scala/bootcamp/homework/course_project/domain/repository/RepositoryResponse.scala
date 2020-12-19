package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

sealed trait RepositoryResponse

sealed trait RepositoryError extends Throwable with RepositoryResponse

case class CreateStationSuccess(stationEntity: StationEntity) extends RepositoryResponse

case class UpdateStationSuccess(stationEntity: StationEntity) extends RepositoryResponse

case class FilterStationSuccess(stationEntities: List[StationEntity]) extends RepositoryResponse

case class DeleteStationSuccess(uniqueName: String) extends RepositoryResponse


case class CreateStationError(errorMessage: String) extends RepositoryError

case class UpdateStationError(errorMessage: String) extends RepositoryError

case class FilterStationError(errorMessage: String) extends RepositoryError

case class DeleteStationError(errorMessage: String) extends RepositoryError
