package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}


sealed trait Priority {
  def value: Int
}

case class High(value: Int = 1) extends Priority {
  override def toString: String = "high"
}

case class Normal(value: Int = 2) extends Priority {
  override def toString: String = "normal"
}

case class Low(value: Int = 3) extends Priority {
  override def toString: String = "low"
}

object PriorityGenericDerivation {

  implicit val encodePriority: Encoder[Priority] = Encoder.instance {
    case urgent @ High(_) => urgent.asJson
    case normal @ Normal(_) => normal.asJson
    case low    @ Low(_)    => low.asJson
  }

  implicit val decodePriority: Decoder[Priority] =
    List[Decoder[Priority]](
      Decoder[High].widen,
      Decoder[Normal].widen,
      Decoder[Low].widen,
    ).reduceLeft(_ or _)

}
