package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto.ValidationError.CityRegionInvalid

sealed trait CityRegion extends Product

case object CityRegion {
  case object Riga extends CityRegion
  case object Daugavpils extends CityRegion
  case object Liepaja  extends CityRegion
  case object Jelgava extends CityRegion
  case object Jurmala extends CityRegion
  case object Ventspils  extends CityRegion
  case object Rezekne  extends CityRegion
  case object Jekabpils extends CityRegion
  case object Valmiera extends CityRegion
  case object Ogre extends CityRegion
  case object Cesis extends CityRegion

  val cityRegions: Map[String, CityRegion] = Set(
    Riga,
    Daugavpils,
    Liepaja,
    Jelgava,
    Jurmala,
    Ventspils,
    Rezekne,
    Jekabpils,
    Valmiera,
    Ogre,
    Cesis
  ).map { x =>
    x.productPrefix.toLowerCase -> x
  }.toMap


  def from(cityRegion: String): Either[ValidationError, CityRegion] = cityRegions.get(cityRegion.toLowerCase) match {
    case Some(cityRegion) => cityRegion.asRight
    case _                => CityRegionInvalid.asLeft
  }

}

