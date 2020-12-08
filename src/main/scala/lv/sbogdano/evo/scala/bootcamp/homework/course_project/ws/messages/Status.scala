package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status.{All, Completed, Pending}

sealed trait Status
object Status {
  case class Completed() extends Status
  case class Pending() extends Status
  case class All() extends Status
}

object StatusGenericDerivation {
  implicit val encodeStatus: Encoder[Status] = Encoder.instance {
    case completed @ Completed() => completed.asJson
    case pending @ Pending() =>pending.asJson
    case all @ All() => all.asJson
  }

  implicit val decodeStatus: Decoder[Status] =
    List[Decoder[Status]](
      Decoder[Completed].widen,
      Decoder[Pending].widen,
      Decoder[All].widen
    ).reduceLeft(_ or _)
}