package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto.ValidationError.LatitudeInvalid

import scala.util.{Failure, Success, Try}

final case class Latitude private(coordinates: Double) {
  override def toString: String = coordinates.toString
}

object Latitude {
  def from(x: String): Either[ValidationError, Latitude] = {
    Try(x.toDouble) match {
      case Failure(_)     => LatitudeInvalid.asLeft
      case Success(value) => Latitude(value).asRight
    }
  }
}
