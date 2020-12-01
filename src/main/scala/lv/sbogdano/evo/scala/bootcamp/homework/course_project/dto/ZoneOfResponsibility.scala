package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto.ValidationError.ZoneOfResponsibilityInvalid

sealed trait ZoneOfResponsibility extends Product

case object ZoneOfResponsibility {
  case object Latgale extends ZoneOfResponsibility
  case object Kurzeme extends ZoneOfResponsibility
  case object Zemgale extends ZoneOfResponsibility
  case object Vidzeme extends ZoneOfResponsibility

  val zoneOfResponsibilities: Map[String, ZoneOfResponsibility] = Set(Latgale, Kurzeme, Zemgale, Vidzeme).map { x =>
    x.productPrefix.toLowerCase -> x
  }.toMap

  def from(zoneOfResponsibility: String): Either[ValidationError, ZoneOfResponsibility] =
    zoneOfResponsibilities.get(zoneOfResponsibility.toLowerCase) match {
      case Some(zoneOfResponsibility) => zoneOfResponsibility.asRight
      case None                       => ZoneOfResponsibilityInvalid.asLeft
    }
}
