package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.validator

import cats.implicits.{catsSyntaxValidatedId, catsSyntaxValidatedIdBinCompat0}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.CityRegion.Riga
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.Construction.Indoor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.ObjectType.TP
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.ZoneOfResponsibility.Latgale
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StationValidatorSpec extends AnyFlatSpec with Matchers {

  "StationValidator" should "handle valid Stations" in new Scope {
    StationValidator.validate(
      "streetName",
      "123",
      "indoor",
      "1978",
      "1234",
      "TP",
      "34",
      "Riga",
      "1234.45",
      "5678.67",
      "Latgale"
    ) shouldBe StationValidated(
      StationAddress(StreetName("streetName"), StreetNumber(123)),
      Indoor,
      Year(1978),
      Year(1234),
      Name(TP, ObjectNumber(34)),
      Riga,
      Latitude(1234.45),
      Longitude(5678.67),
      Latgale
    ).validNec
  }

  it should "contain StreetNameInvalidFormat when invalid streetName provided" in new Scope {

    checkInvalid(
      streetName = "streetName1",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "1999",
      objectType = "tp",
      objectNumber = "12",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale"
    ) shouldBe Set(StreetNameInvalidFormat).invalid
  }


  it should "contain StreetNumberInvalidFormat, ConstructionInvalid when invalid streetNumber and construction were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123a",
      construction = "indoor1",
      yearOfManufacture = "2001",
      inServiceFrom = "1999",
      objectType = "tp",
      objectNumber = "12",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale"
    ) shouldBe Set(StreetNumberInvalidFormat, ConstructionInvalid).invalid
  }

  it should "contain StreetNumberIsNegative, YearInvalidFormat when invalid streetNumber and yearOfManufacture were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "-1",
      construction = "indoor",
      yearOfManufacture = "200a",
      inServiceFrom = "1999",
      objectType = "tp",
      objectNumber = "12",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(StreetNumberIsNegative, YearInvalidFormat).invalid
  }

  it should "contain ConstructionInvalid, YearInvalidLength when invalid construction and yearOfManufacture were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "",
      yearOfManufacture = "20001",
      inServiceFrom = "1999",
      objectType = "tp",
      objectNumber = "12",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(ConstructionInvalid, YearInvalidLength).invalid
  }

  it should "contain YearInvalidLength, ObjectTypeInvalid when invalid inServiceFrom and objectType were provided" in new Scope {
    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "19991",
      objectType = "tp1",
      objectNumber = "12",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(YearInvalidLength, ObjectTypeInvalid).invalid

  }

  it should "contain YearInvalidFormat, ObjectNumberInvalidFormat when invalid inServiceFrom and objectNumber were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "199a",
      objectType = "ktp",
      objectNumber = "12a",
      cityRegion = "riga",
      latitude = "1234.45",
      longitude = "456.67",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(YearInvalidFormat, ObjectNumberInvalidFormat).invalid

  }


  it should "contain ObjectNumberInvalidLength, CityRegionInvalid, LatitudeInvalid when invalid objectNumber, cityRegion and latitude were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "1999",
      objectType = "ktp",
      objectNumber = "12345",
      cityRegion = "dagda",
      latitude = "1234.45a",
      longitude = "456.67",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(ObjectNumberInvalidLength, CityRegionInvalid, LatitudeInvalid).invalid
  }

  it should "contain ObjectNumberInvalidLength, CityRegionInvalid, LongitudeInvalid when invalid objectNumber, cityRegion and longitude were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "1999",
      objectType = "ktp",
      objectNumber = "12345",
      cityRegion = "dagda",
      latitude = "1234.45",
      longitude = "",
      zoneOfResponsibility = "latgale",
    ) shouldBe Set(ObjectNumberInvalidLength, CityRegionInvalid, LongitudeInvalid).invalid
  }

  it should "contain CityRegionInvalid, ZoneOfResponsibilityInvalid when invalid cityRegion and zoneOfResponsibility were provided" in new Scope {

    checkInvalid(
      streetName = "streetName",
      streetNumber = "123",
      construction = "indoor",
      yearOfManufacture = "2001",
      inServiceFrom = "1999",
      objectType = "ktp",
      objectNumber = "1234",
      cityRegion = "dagda",
      latitude = "1234.45",
      longitude = "456",
      zoneOfResponsibility = "invalidZone",
    ) shouldBe Set(CityRegionInvalid, ZoneOfResponsibilityInvalid).invalid
  }

  it should "contain StreetNameInvalidFormat, ConstructionInvalid, YearInvalidLength, ObjectTypeInvalid, CityRegionInvalid, LatitudeInvalid, LongitudeInvalid, ZoneOfResponsibilityInvalid" +
    " when invalid streetName, construction, yearOfManufacture, objectType, cityRegion, latitude, longitude and zoneOfResponsibility were provided" in new Scope {

    checkInvalid(
      streetName = "invalid1",
      streetNumber = "123",
      construction = "ivalid",
      yearOfManufacture = "invalid",
      inServiceFrom = "2000",
      objectType = "invalid",
      objectNumber = "invalid",
      cityRegion = "invalid",
      latitude = "invalid",
      longitude = "invalid",
      zoneOfResponsibility = "invalid",
    ) shouldBe Set(
      StreetNameInvalidFormat,
      ConstructionInvalid,
      YearInvalidLength,
      ObjectTypeInvalid,
      CityRegionInvalid,
      LatitudeInvalid,
      LongitudeInvalid,
      ZoneOfResponsibilityInvalid
    ).invalid
  }

  trait Scope {
    def checkInvalid(
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
                    zoneOfResponsibility: String,
                    ) = {
      StationValidator.validate(
        streetName = streetName,
        streetNumber = streetNumber,
        construction = construction,
        yearOfManufacture = yearOfManufacture,
        inServiceFrom = inServiceFrom,
        objectType = objectType,
        objectNumber = objectNumber,
        cityRegion = cityRegion,
        latitude = latitude,
        longitude = longitude,
        zoneOfResponsibility = zoneOfResponsibility
      ).leftMap(_.toChain.toList.toSet)
    }
  }


}
