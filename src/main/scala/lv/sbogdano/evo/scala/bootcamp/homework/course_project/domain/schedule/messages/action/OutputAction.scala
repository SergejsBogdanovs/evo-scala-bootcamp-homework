package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.JobSchedule

sealed trait OutputAction
case class WelcomeUser(message: String) extends OutputAction
case class UserJobSchedule(jobSchedule: JobSchedule) extends OutputAction
case class DisconnectResult(message: String) extends OutputAction
case class UpdateJobResult(updatedRows: Int) extends OutputAction

sealed trait OutputActionError extends OutputAction
case class FindJobsError(errorMessage: String) extends OutputActionError
case class UpdateJobError(errorMessage: String) extends OutputActionError
case class AddJobError(errorMessage: String) extends OutputActionError
case class DeleteJobError(errorMessage: String) extends OutputActionError
case class InvalidInputError(errorMessage: String) extends OutputActionError
case class SystemError(errorMessage: String) extends OutputActionError

object OutputActionGenericDerivation {

  implicit val encodeOutputAction: Encoder[OutputAction] = Encoder.instance {
    case welcomeUser      @ WelcomeUser(_)   => welcomeUser.asJson
    case userJobSchedule  @ UserJobSchedule(_)  => userJobSchedule.asJson
    case updateJobResult  @ UpdateJobResult(_)  => updateJobResult.asJson
    case disconnectResult @ DisconnectResult(_) => disconnectResult.asJson

    case findJobsError     @ FindJobsError(_)     => findJobsError.asJson
    case updateJobsError   @ UpdateJobError(_)    => updateJobsError.asJson
    case addJobError       @ AddJobError(_)       => addJobError.asJson
    case deleteJobError    @ DeleteJobError(_)    => deleteJobError.asJson
    case invalidInputError @ InvalidInputError(_) => invalidInputError.asJson
    case systemError       @ SystemError(_)       => systemError.asJson
  }

  implicit val decodeOutputAction: Decoder[OutputAction] =
    List[Decoder[OutputAction]](
      Decoder[WelcomeUser].widen,
      Decoder[UserJobSchedule].widen,
      Decoder[UpdateJobResult].widen,
      Decoder[OutputActionError].widen,
      Decoder[SystemError].widen,
    ).reduceLeft(_ or _)
}

