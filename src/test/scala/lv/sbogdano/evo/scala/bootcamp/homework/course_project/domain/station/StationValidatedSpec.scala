package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.CityRegion.Riga
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.Construction.Indoor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.ObjectType.TP
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.ZoneOfResponsibility.Latgale
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StationValidatedSpec extends AnyFlatSpec with Matchers {

  "StationValidate" should " be converted to StationEntity" in {

    val stationValidated = StationValidated(
      StationAddress(StreetName("streetName"), StreetNumber(123)),
      Indoor,
      Year(1978),
      Year(1990),
      Name(TP, ObjectNumber(34)),
      Riga,
      Latitude(1234.45),
      Longitude(5678.67),
      Latgale
    )

    stationValidated.toStationEntity shouldBe StationEntity(
      uniqueName = "Riga_TP34",
      stationAddress = "streetName 123",
      construction = "Indoor",
      yearOfManufacture = 1978,
      inServiceFrom = 1990,
      name = "TP34",
      cityRegion = "Riga",
      latitude = 1234.45,
      longitude = 5678.67,
      zoneOfResponsibility = "Latgale"
    )
  }
}
