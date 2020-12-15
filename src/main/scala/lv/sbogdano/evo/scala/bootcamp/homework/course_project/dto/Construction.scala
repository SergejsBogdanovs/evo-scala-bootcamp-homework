package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId

sealed trait Construction extends Product

case object Construction {
  case object Indoor extends Construction
  case object Outdoor extends Construction

  val constructions: Map[String, Construction] = Set(Indoor, Outdoor).map { x =>
    x.productPrefix.toLowerCase -> x
  }.toMap
  
  def from(construction: String): Either[ValidationError, Construction] = constructions.get(construction.toLowerCase) match {
    case Some(construction) => construction.asRight
    case None               => ConstructionInvalid.asLeft
  }
}
