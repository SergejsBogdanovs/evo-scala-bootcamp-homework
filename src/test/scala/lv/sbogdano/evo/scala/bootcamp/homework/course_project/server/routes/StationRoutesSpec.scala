package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes

import cats.data.Kleisli
import cats.effect.IO
import fs2.Stream
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import org.http4s._
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class StationRoutesSpec extends AnyFlatSpec with Matchers  {


  // UNAUTHORIZED CREATE
  "HttpService" should "receive 401 Unauthorized error when unauthorized user try CREATE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = postUri,
      body = EmptyBody
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Unauthorized,
      expectedBody = ""
    )
  }

  // UNAUTHORIZED UPDATE
  it should "receive 401 Unauthorized error when unauthorized user try UPDATE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.PUT,
      uri = postUri,
      body = EmptyBody
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Unauthorized,
      expectedBody = ""
    )
  }

  // UNAUTHORIZED DELETE
  it should "receive 401 Unauthorized error when unauthorized user try DELETE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.DELETE,
      uri = postUri,
      body = EmptyBody
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Unauthorized,
      expectedBody = ""
    )
  }

  // INVALID URI
  it should "receive 404 Not Found error when uri is wrong" in new Scope {
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = wrongUri,
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.NotFound,
      expectedBody = "Not found"
    )
  }

  // AUTHORIZED CREATE
  it should "receive Status 201 Created and StationBody as body when authorized user created Station successfully" in new Scope {
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = postUri,
      body = Stream.emits(os = createStationJson.map(_.toByte)),
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Created,
      expectedBody = createStationJson,
    )
  }

  // INVALID UPDATE
  it should "receive Status 400 BadRequest when authorized user try to update Station which is not in Storage" in new Scope {

    val request: Request[IO] = Request(
      method = Method.PUT,
      uri = postUri,
      body = Stream.emits(os = updateStationJson.map(_.toByte)),
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.BadRequest,
      expectedBody = updateStationErrorJson
    )
  }

  // VALID UPDATE
  it should "receive Status 200 Ok and updated StationEntity when authorized user update Station successfully" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    val updateRequest: Request[IO] = Request(
      method = Method.PUT,
      uri = postUri,
      body = Stream.emits(os = updateStationJson.map(_.toByte)),
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = routerUpdate.run(updateRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = updateStationJson
    )
  }

  // AUTHORIZED GET
  it should "receive Status 200 Ok and List[StationEntity] when authorized user get Stations" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    val getRequest: Request[IO] = Request(
      method = Method.GET,
      uri = getUri,
      body = EmptyBody,
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = routerGet.run(getRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = createdStationJsonList
    )
  }

  // UNAUTHORIZED GET
  it should "receive Status 200 Ok and List[StationEntity] when unauthorized user get Stations" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    val getRequest: Request[IO] = Request(
      method = Method.GET,
      uri = getUri,
      body = EmptyBody
    )

    val response: IO[Response[IO]] = routerGet.run(getRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = createdStationJsonList
    )
  }

  // INVALID GET
  it should "receive 404 Not found when user get Stations which are not in Storage" in new Scope {

    val getRequest: Request[IO] = Request(
      method = Method.GET,
      uri = getUri,
      body = EmptyBody
    )

    val response: IO[Response[IO]] = router.run(getRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.NotFound,
      expectedBody = getStationErrorJson
    )
  }

  // INVALID DELETE
  it should "receive Status 400 BadRequest when authorized user try delete Station which is not in Storage" in new Scope {

    val request: Request[IO] = Request(
      method = Method.DELETE,
      uri = deleteUri,
      body = EmptyBody,
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.BadRequest,
      expectedBody = deleteStationErrorJson
    )
  }

  // VALID DELETE
  it should "receive Status 200 Ok and deleted StationEntity uniqueName when authorized user delete Station successfully" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    val deleteRequest: Request[IO] = Request(
      method = Method.DELETE,
      uri = deleteUri,
      body = EmptyBody,
      headers = Headers.of(Header("Authorization", "admin"))
    )

    val response: IO[Response[IO]] = routerUpdate.run(deleteRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = deletedStationResponse
    )
  }

  trait Scope {

    val createStationJson: String =
      """{"uniqueName":"Riga_AS130","stationAddress":"Dammes 6","construction":"indoor","yearOfManufacture":2010,"inServiceFrom":2011,"name":"as130","cityRegion":"Riga","latitude":45.6123,"longitude":12.3456,"zoneOfResponsibility":"Latgale"}""".stripMargin
    val createdStationJsonList: String =
      """[{"uniqueName":"Riga_AS130","stationAddress":"Dammes 6","construction":"indoor","yearOfManufacture":2010,"inServiceFrom":2011,"name":"as130","cityRegion":"Riga","latitude":45.6123,"longitude":12.3456,"zoneOfResponsibility":"Latgale"}]""".stripMargin
    val updateStationJson: String =
      """{"uniqueName":"Riga_AS130","stationAddress":"Rigas 6","construction":"outdoor","yearOfManufacture":2008,"inServiceFrom":2010,"name":"as130","cityRegion":"Riga","latitude":45.6123,"longitude":12.3456,"zoneOfResponsibility":"Kurzeme"}""".stripMargin

    val deletedStationResponse: String = """"Riga_AS130"""".stripMargin

    val updateStationErrorJson: String = """{"UpdateStationError":{"errorMessage":"Not found station to update"}}""".stripMargin
    val deleteStationErrorJson: String = """{"DeleteStationError":{"errorMessage":"Not found station to delete"}}""".stripMargin
    val getStationErrorJson: String = """{"FilterStationError":{"errorMessage":"Not found any station"}}""".stripMargin

    val router: Kleisli[IO, Request[IO], Response[IO]] =  StationRoutes.makeRouter(CacheStorage())

    val postUri = uri"/api/v1/admin/stations"
    val deleteUri = uri"/api/v1/admin/stations/Riga_AS130"
    val getUri = uri"/api/v1/user/stations/as130"
    val wrongUri = uri"/api/v2/admin/stations"

    val stationMock: StationEntity = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Dammes 6",
      construction = "indoor",
      yearOfManufacture = 2010,
      inServiceFrom = 2011,
      name = "as130",
      cityRegion = "Riga",
      latitude = 45.6123,
      longitude = 12.3456,
      zoneOfResponsibility = "Latgale"
    )

    def check[A](
                actualResponseIO: IO[Response[IO]],
                expectedStatus: Status,
                expectedBody: String
                )(implicit decoder: EntityDecoder[IO, A]): Unit = (
      for {
        actualResponse <- actualResponseIO
        _              <- IO(actualResponse.status shouldBe expectedStatus)
        _              <- actualResponse.as[A].map(_ shouldBe expectedBody)
      } yield ()).unsafeRunSync()
  }

}
