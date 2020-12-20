package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CacheStorageSpec extends AnyFlatSpec with Matchers {

  "CacheStorage" should "return CacheCreateStationSuccess when createStation() called successfully" in new Scope {
    val result: Either[CreateStationError, CacheCreateStationSuccess] = storageEmpty.createStation(stationMock)
    result shouldEqual Right(CacheCreateStationSuccess(List(stationMock)))
  }

  it should "return CreateStationError when station already exist" in new Scope {
    val result: Either[CreateStationError, CacheCreateStationSuccess] = storageFull.createStation(stationMock)
    result shouldEqual Left(CreateStationError("Already exist"))
  }

  it should "return CacheUpdateStationSuccess when updateStation() called successfully" in new Scope {
    val result: Either[UpdateStationError, CacheUpdateStationSuccess] = storageFull.updateStation(stationMockToUpdate)
    result shouldEqual Right(CacheUpdateStationSuccess(List(stationMockToUpdate, stationMock1)))
  }

  it should "return UpdateStationError when stations cache do not contain value to update" in new Scope {
    val result: Either[UpdateStationError, CacheUpdateStationSuccess] = storageFull.updateStation(notContainedStationMockToUpdate)
    result shouldEqual Left(UpdateStationError("Not found station to update"))
  }

  it should "return FilterStationSuccess when filterStations() called successfully" in new Scope {
    val result: Either[FilterStationError, FilterStationSuccess] = storageFull.filterStations("AS130")
    result shouldEqual Right(FilterStationSuccess(List(stationMock, stationMock1)))
  }

  it should "return FilterStationError when filterStations() called with invalid name " in new Scope {
    val result: Either[FilterStationError, FilterStationSuccess] = storageFull.filterStations("TP130")
    result shouldEqual Left(FilterStationError("Not found any station"))
  }

  it should "return CacheDeleteStationSuccess when deleteStation() called successfully" in new Scope {
    val result: Either[DeleteStationError, CacheDeleteStationSuccess] = storageFull.deleteStation("Riga_AS130")
    result shouldEqual Right(CacheDeleteStationSuccess(List(stationMock1)))
  }

  it should "return DeleteStationError when not found station to delete" in new Scope {
    val result: Either[DeleteStationError, CacheDeleteStationSuccess] = storageFull.deleteStation("Riga_AS1")
    result shouldEqual Left(DeleteStationError("Not found station to delete"))
  }


  trait Scope {

    val stationMock: StationEntity = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Stadiona 1",
      construction = "indoor",
      yearOfManufacture = 2011,
      inServiceFrom = 2020,
      name = "AS130",
      cityRegion = "Riga",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "Vidzeme"
    )

    val stationMock1: StationEntity = StationEntity(
      uniqueName = "Rezekne_AS130",
      stationAddress = "Rezeknes 1",
      construction = "outdoor",
      yearOfManufacture = 1990,
      inServiceFrom = 1994,
      name = "AS130",
      cityRegion = "Rezekne",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "Latgale"
    )

    val stationMockToUpdate: StationEntity = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Brivibas 1",
      construction = "outdoor",
      yearOfManufacture = 2019,
      inServiceFrom = 2020,
      name = "AS130",
      cityRegion = "Riga",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "Vidzeme"
    )

    val notContainedStationMockToUpdate: StationEntity = StationEntity(
      uniqueName = "Daugavpils_AS13",
      stationAddress = "address",
      construction = "construction",
      yearOfManufacture = 2011,
      inServiceFrom = 2020,
      name = "name",
      cityRegion = "cityRegion",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "zoneOfResponsibility"
    )

    val storageEmpty: CacheStorage = CacheStorage()
    val storageFull: CacheStorage = CacheStorage(null, List(stationMock, stationMock1))
  }

}
