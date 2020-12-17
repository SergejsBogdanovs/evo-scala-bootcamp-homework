package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CacheStorageSpec extends AnyFlatSpec with Matchers {

  "CacheStorage" should "have empty list at start" in new Scope {
    val stations: List[StationEntity] = storage.stations
    stations shouldEqual List.empty
  }

  it should "return CreateStationSuccess when createStation() called successfully" in new Scope {
    val created: Either[CreateStationError, CreateStationSuccess] = storage.createStation(stationMock).unsafeRunSync()
    created shouldEqual Right(CreateStationSuccess(stationMock))
    storage.stations.size shouldEqual 1
  }

  it should "return UpdateStationSuccess when updateStation() called successfully" in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    val updated: Either[UpdateStationError, UpdateStationSuccess] = storage.updateStation(stationMockToUpdate).unsafeRunSync()
    updated shouldEqual Right(UpdateStationSuccess(stationMockToUpdate))
  }

  it should "return UpdateStationError when stations cache do not contain value to update" in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    val updated: Either[UpdateStationError, UpdateStationSuccess] = storage.updateStation(notContainedStationMockToUpdate).unsafeRunSync()
    updated shouldEqual Left(UpdateStationError("Not found station to update"))
  }

  it should "return FilterStationSuccess when filterStations() called successfully" in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    storage.createStation(stationMock1).unsafeRunSync()
    val stations: Either[FilterStationError, FilterStationSuccess] = storage.filterStations("AS130").unsafeRunSync()
    stations shouldEqual Right(FilterStationSuccess(List(
      StationEntity(uniqueName = "Riga_AS130", stationAddress = "Stadiona 1", construction = "indoor", yearOfManufacture = 2011, inServiceFrom = 2020, name = "AS130", cityRegion = "Riga", latitude = 123.45, longitude = 567.89, zoneOfResponsibility = "Vidzeme"),
      StationEntity(uniqueName = "Rezekne_AS130", stationAddress = "Rezeknes 1", construction = "outdoor", yearOfManufacture = 1990, inServiceFrom = 1994, name = "AS130", cityRegion = "Rezekne", latitude = 123.45, longitude = 567.89, zoneOfResponsibility = "Latgale")
    )))
  }

  it should "return FilterStationError when filterStations() called with invalid name " in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    storage.createStation(stationMock1).unsafeRunSync()
    val stations: Either[FilterStationError, FilterStationSuccess] = storage.filterStations("TP130").unsafeRunSync()
    stations shouldEqual Left(FilterStationError("Not found any station"))
  }

  it should "return DeleteStationSuccess when deleteStation() called successfully" in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    val uniqueName: Either[DeleteStationError, DeleteStationSuccess] = storage.deleteStation("Riga_AS130").unsafeRunSync()
    uniqueName shouldEqual Right(DeleteStationSuccess("Riga_AS130"))
    storage.stations.isEmpty shouldEqual true
  }

  it should "return DeleteStationError when not found station to delete" in new Scope {
    storage.createStation(stationMock).unsafeRunSync()
    val uniqueName: Either[DeleteStationError, DeleteStationSuccess] = storage.deleteStation("Riga_AS1").unsafeRunSync()
    uniqueName shouldEqual Left(DeleteStationError("Not found station to delete"))
  }


  trait Scope {

    val storage: CacheStorage = CacheStorage()

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
  }

}
