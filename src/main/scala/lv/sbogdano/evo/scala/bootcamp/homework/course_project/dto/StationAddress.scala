package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto.ValidationError.{StreetNameInvalidFormat, StreetNumberInvalidFormat, StreetNumberIsNegative}

import scala.util.{Failure, Success, Try}

final case class StationAddress(streetName: StreetName, streetNumber: StreetNumber) {
  override def toString: String = s"${streetName.streetName} ${streetNumber.streetNumber}"
}

final case class StreetName private(streetName: String)

object StreetName {
  def from(streetName: String): Either[ValidationError, StreetName] = {
    val reg = "^[A-Za-z]+$"
    if (streetName.matches(reg)) StreetName(streetName).asRight else StreetNameInvalidFormat.asLeft
  }

}

final case class StreetNumber private(streetNumber: Int)

object StreetNumber {
  def from(streetNumber: String): Either[ValidationError, StreetNumber] = {
    Try(streetNumber.toInt) match {
      case Failure(_)     => StreetNumberInvalidFormat.asLeft
      case Success(value) => if (value > 0) StreetNumber(value).asRight else StreetNumberIsNegative.asLeft
    }
  }
}
