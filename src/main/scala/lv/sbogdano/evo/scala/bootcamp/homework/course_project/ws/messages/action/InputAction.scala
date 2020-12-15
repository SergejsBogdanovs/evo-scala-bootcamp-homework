package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}

sealed trait InputAction
case object EnterJobSchedule extends InputAction
case object FindJobsByUser extends InputAction
case class FindJobsByUserAndStatus(status: Status) extends InputAction
case class AddJobToSchedule(job: Job) extends InputAction
case class UpdateJobStatus(jobId: Int, newStatus: Status) extends InputAction
case class UpdateJobPriority(jobId: Int, newPriority: Priority) extends InputAction
case class DeleteJobFromSchedule(job: Job) extends InputAction
case object InvalidInput extends InputAction
case object Disconnect extends InputAction


object InputActionGenericDerivation {


  implicit val encodeInputAction: Encoder[InputAction] = Encoder.instance {
//    case enterJobSchedule  @ EnterJobSchedule        => enterJobSchedule.asJson
//    case findJobsByUser    @ FindJobsByUser          => findJobsByUser.asJson
    case findJobsByUserAndStatus @ FindJobsByUserAndStatus(_) => findJobsByUserAndStatus.asJson
    case addJobsToSchedule       @ AddJobToSchedule(_)        => addJobsToSchedule.asJson
    case updateJobStatus         @ UpdateJobStatus(_, _)      => updateJobStatus.asJson
    case updateJobPriority       @ UpdateJobPriority(_, _)    => updateJobPriority.asJson
    case deleteJobFromSchedule   @ DeleteJobFromSchedule(_)   => deleteJobFromSchedule.asJson
//    case invalidInput      @ InvalidInput            => invalidInput.asJson
  }

  implicit val decodeInputAction: Decoder[InputAction] =
    List[Decoder[InputAction]](
//      Decoder[EnterJobSchedule].widen,
//      Decoder[FindJobsByUser].widen,
      Decoder[FindJobsByUserAndStatus].widen,
      Decoder[AddJobToSchedule].widen,
      Decoder[UpdateJobStatus].widen,
      Decoder[UpdateJobPriority].widen,
      Decoder[DeleteJobFromSchedule].widen,
//      Decoder[InvalidInput].widen,
    ).reduceLeft(_ or _)
}

