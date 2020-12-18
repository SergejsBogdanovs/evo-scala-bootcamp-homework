package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station

import cats.implicits.catsSyntaxEitherId

import scala.util.{Failure, Success, Try}

final case class Year private (year: Int) extends AnyVal {
  override def toString: String = s"$year"
}
object Year {
  def from(year: String): Either[ValidationError, Year] = {

    if (year.length == 4) {
      Try(year.toInt) match {
        case Failure(_)     => YearInvalidFormat.asLeft
        case Success(value) => Year(value).asRight
      }
    } else {
      YearInvalidLength.asLeft
    }
  }

}
