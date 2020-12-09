package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.InputAction._

sealed trait InputAction
object InputAction {
  case class EnterJobScheduleInputAction() extends InputAction
  case class ListJobsInputAction(status: Status) extends InputAction
  case class AddJobsInputAction(toUser: UserLogin, stationEntities: List[StationEntity]) extends InputAction
  case class MarkJobAsCompletedInputAction(stationEntity: StationEntity) extends InputAction
  case class InvalidInputInputAction() extends InputAction
}

object ActionGenericDerivation {


  implicit val encodeAction: Encoder[InputAction] = Encoder.instance {
    case enterJobScheduleAction @ EnterJobScheduleInputAction()      => enterJobScheduleAction.asJson
    case listJobsAction @ ListJobsInputAction(_)                     => listJobsAction.asJson
    case addJobsAction @ AddJobsInputAction(_, _)                    => addJobsAction.asJson
    case markJobAsCompletedAction @ MarkJobAsCompletedInputAction(_) => markJobAsCompletedAction.asJson
    case invalidInputAction @ InvalidInputInputAction()              => invalidInputAction.asJson
  }

  implicit val decodeAction: Decoder[InputAction] =
    List[Decoder[InputAction]](
      Decoder[EnterJobScheduleInputAction].widen,
      Decoder[ListJobsInputAction].widen,
      Decoder[AddJobsInputAction].widen,
      Decoder[MarkJobAsCompletedInputAction].widen,
      Decoder[InvalidInputInputAction].widen,
    ).reduceLeft(_ or _)
}

