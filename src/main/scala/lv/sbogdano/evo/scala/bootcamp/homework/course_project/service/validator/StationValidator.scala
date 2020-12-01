package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.validator

import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple9Semigroupal, catsSyntaxValidatedId, catsSyntaxValidatedIdBinCompat0}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto._

object StationValidator {

  type ErrorMessage = String

  type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

  private def validateStationAddress(streetName: String, streetNumber: String): AllErrorsOr[StationAddress] = {
    StreetName.from(streetName) match {
      case Left(error) => error.invalidNec
      case Right(name) => StreetNumber.from(streetNumber) match {
        case Left(error)   => error.invalidNec
        case Right(number) => StationAddress(name, number).valid
      }
    }
  }

  private def validateConstruction(construction: String): AllErrorsOr[Construction] = {
    Construction.from(construction) match {
      case Left(error)         => error.invalidNec
      case Right(construction) => construction.valid
    }
  }

  private def validateYearOfManufacture(year: String): AllErrorsOr[Year] = {
    Year.from(year) match {
      case Left(error) => error.invalidNec
      case Right(year) => year.valid
    }
  }

  private def validateInServiceFrom(year: String): AllErrorsOr[Year] = {
    Year.from(year) match {
      case Left(error) => error.invalidNec
      case Right(year) => year.valid
    }
  }

  private def validateName(objectType: String, objectNumber: String): AllErrorsOr[Name] = {
    ObjectType.from(objectType) match {
      case Left(error)       => error.invalidNec
      case Right(objectType) => ObjectNumber.from(objectNumber) match {
        case Left(error)         => error.invalidNec
        case Right(objectNumber) => Name(objectType, objectNumber).valid
      }
    }
  }

  private def validateCityRegion(cityRegion: String): AllErrorsOr[CityRegion] = {
    CityRegion.from(cityRegion) match {
      case Left(error)       => error.invalidNec
      case Right(cityRegion) => cityRegion.valid
    }
  }

  private def validateStationLatitude(coordinates: String): AllErrorsOr[Latitude] = {
    Latitude.from(coordinates) match {
      case Left(error)     => error.invalidNec
      case Right(latitude) => latitude.valid
    }
  }

  private def validateStationLongitude(coordinates: String): AllErrorsOr[Longitude] = {
    Longitude.from(coordinates) match {
      case Left(error)      => error.invalidNec
      case Right(longitude) => longitude.valid
    }
  }

  private def validateZoneOfResponsibility(zoneOfResponsibility: String): AllErrorsOr[ZoneOfResponsibility] = {
    ZoneOfResponsibility.from(zoneOfResponsibility) match {
      case Left(error)                 => error.invalidNec
      case Right(zoneOfResponsibility) => zoneOfResponsibility.valid
    }
  }

  def validate(
                streetName: String,
                streetNumber: String,
                construction: String,
                yearOfManufacture: String,
                inServiceFrom: String,
                objectType: String,
                objectNumber: String,
                cityRegion: String,
                latitude: String,
                longitude: String,
                zoneOfResponsibility: String
              ): AllErrorsOr[StationValidated] = {

    ( validateStationAddress(streetName, streetNumber),
      validateConstruction(construction),
      validateYearOfManufacture(yearOfManufacture),
      validateInServiceFrom(inServiceFrom),
      validateName(objectType, objectNumber),
      validateCityRegion(cityRegion),
      validateStationLatitude(latitude),
      validateStationLongitude(longitude),
      validateZoneOfResponsibility(zoneOfResponsibility)
      ).mapN(StationValidated)
  }
}
