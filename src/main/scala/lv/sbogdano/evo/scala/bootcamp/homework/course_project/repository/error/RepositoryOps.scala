package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity

sealed trait RepositoryOps

object RepositoryOps {
  case class CreateStationSuccess(stationEntity: StationEntity) extends RepositoryOps
  case class UpdateStationSuccess(stationEntity: StationEntity) extends RepositoryOps
  case class FilterStationSuccess(stationEntities: List[StationEntity]) extends RepositoryOps
  case class DeleteStationSuccess(uniqueName: String) extends RepositoryOps

  case class CreateStationError(errorMessage: String) extends RepositoryOps
  case class UpdateStationError(errorMessage: String) extends RepositoryOps
  case class FilterStationError(errorMessage: String) extends RepositoryOps
  case class DeleteStationError(errorMessage: String) extends RepositoryOps
}
