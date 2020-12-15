package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId

import scala.util.{Failure, Success, Try}

final case class Longitude private(coordinates: Double) {
  override def toString: String = coordinates.toString
}

object Longitude {
  def from(y: String): Either[ValidationError, Longitude] = {
    Try(y.toDouble) match {
      case Failure(_)     => LongitudeInvalid.asLeft
      case Success(value) => Longitude(value).asRight
    }
  }
}
