package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error

sealed trait RepositoryOpsError

object RepositoryOpsError {
  case class CreateStationError(errorMessage: String) extends RepositoryOpsError

  case class UpdateStationError(errorMessage: String) extends RepositoryOpsError

  case class FilterStationError(errorMessage: String) extends RepositoryOpsError

  case class DeleteStationError(errorMessage: String) extends RepositoryOpsError
}
