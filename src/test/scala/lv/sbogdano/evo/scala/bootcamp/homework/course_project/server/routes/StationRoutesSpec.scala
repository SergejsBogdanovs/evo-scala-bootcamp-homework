package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import fs2.Stream
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.User
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import org.http4s._
import org.http4s.headers.`Set-Cookie`
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class StationRoutesSpec extends AnyFlatSpec with Matchers {

  // LOGIN ADMIN
  "HttpService" should "receive authcookie = admin, when login as admin" in new Scope {

    val request: Request[IO] = Request(
      method = Method.POST,
      uri = loginUri,
      body = Stream.emits(os = loginAdminJson.map(_.toByte))
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = "Logged in",
      expectedResponseCookie = ResponseCookie("authcookie", "admin").some
    )
  }

  // LOGIN WORKER
  "HttpService" should "receive authcookie = worker, when login as worker" in new Scope {

    val request: Request[IO] = Request(
      method = Method.POST,
      uri = loginUri,
      body = Stream.emits(os = loginWorkerJson.map(_.toByte))
    )

    val response: IO[Response[IO]] = router.run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Status.Ok,
      expectedBody = "Logged in",
      expectedResponseCookie = ResponseCookie("authcookie", "worker").some
    )
  }

  // UNAUTHORIZED CREATE
  "HttpService" should "receive Status 401 Unauthorized error when unauthorized user try CREATE Station" in new Scope {
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
  it should "receive Status 401 Unauthorized error when unauthorized user try UPDATE Station" in new Scope {
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
  it should "receive Status 401 Unauthorized error when unauthorized user try DELETE Station" in new Scope {
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

  // UNAUTHORIZED GET
  it should "receive Status 401 Unauthorized when unauthorized user GET Stations" in new Scope {

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
      expectedStatus = Status.Unauthorized,
      expectedBody = ""
    )
  }

  // INVALID URI
  it should "receive Status 404 Not Found error when uri is wrong" in new Scope {
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

  // AUTHORIZED CREATE as ADMIN
  it should "receive Status 201 Created and StationBody as body when ADMIN created Station successfully" in new Scope {

    login(loginAdminJson) match {
      case Some(responseCookie) => {
        val request: Request[IO] = Request(
          method = Method.POST,
          uri = postUri,
          body = Stream.emits(os = stationMockJson.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Created,
          expectedBody = stationMockJson,
        )
      }
      case None => "Can not find authcookie"
    }
  }

  // AUTHORIZED CREATE as WORKER
  it should "receive Status 401 Unauthorized when Worker try CREATE Station" in new Scope {

    login(loginWorkerJson) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.POST,
          uri = postUri,
          body = Stream.emits(os = stationMockJson.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
          expectedBody = "",
        )
      case None => "Can not find authcookie"
    }
  }

  // INVALID UPDATE. ADMIN
  it should "receive Status 404 Not found when authorized ADMIN try to UPDATE Station which is not in Storage" in new Scope {

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = updateStationErrorJson
        )
      case None => "Can not find authcookie"
    }
  }

  // VALID UPDATE. ADMIN
  it should "receive Status 200 Ok and updated StationEntity when ADMIN UPDATE Station successfully" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val updateRequest: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerUpdate.run(updateRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = updateStationMockJson
        )

      case None => "Can not find authcookie"
    }

  }

  // INVALID UPDATE. WORKER
  it should "receive Status 401 Unauthorized when WORKER try UPDATE Station" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginWorkerJson) match {
      case Some(responseCookie) =>
        val updateRequest: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerUpdate.run(updateRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
          expectedBody = ""
        )

      case None => "Can not find authcookie"
    }
  }

  // AUTHORIZED GET. ADMIN
  it should "receive Status 200 Ok and List[StationEntity] when ADMIN GET Stations" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerGet.run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = stationsMockJson
        )
      case None => "Can not find authcookie"
    }
  }

  // AUTHORIZED GET. WORKER
  it should "receive Status 200 Ok and List[StationEntity] when authorized WORKER GET Stations" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginWorkerJson) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerGet.run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = stationsMockJson
        )

      case None => "Can not find authcookie"
    }
  }

  // INVALID GET. ADMIN
  it should "receive Status 404 Not found when ADMIN try get Stations which are not in Storage" in new Scope {

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = getStationErrorJson
        )
      case None => "Can not find authcookie"
    }
  }

  // INVALID DELETE
  it should "receive Status 404 Not Found when ADMIN try delete Station which is not in Storage" in new Scope {

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = deleteStationErrorJson
        )
      case None => "Can not find authcookie"
    }
  }

  // VALID DELETE. ADMIN
  it should "receive Status 200 Ok and deleted StationEntity uniqueName when ADMIN delete Station successfully" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerCreate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginAdminJson) match {
      case Some(responseCookie) =>
        val deleteRequest: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerCreate.run(deleteRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = deletedStationResponse
        )
      case None => "Can not find authcookie"
    }
  }

  // INVALID DELETE. WORKER
  it should "receive Status 401 Unauthorized when WORKER try DELETE Station" in new Scope {

    // Prepopulate Storage
    val cacheStorage: CacheStorage = CacheStorage()
    cacheStorage.createStation(stationMock)
    val routerCreate: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(cacheStorage)

    login(loginWorkerJson) match {
      case Some(responseCookie) =>
        val deleteRequest: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          body = EmptyBody,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerCreate.run(deleteRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
          expectedBody = ""
        )
      case None => "Can not find authcookie"
    }
  }

  trait Scope {

    import io.circe.generic.auto._

    val loginAdminJson: String = User(login = "admin", password = "admin", role = "admin").asJson.noSpaces
    val loginWorkerJson: String = User(login = "worker", password = "worker", role = "worker").asJson.noSpaces

    val deletedStationResponse: String = """"Riga_AS130"""".stripMargin
    val updateStationErrorJson: String = """{"UpdateStationError":{"errorMessage":"Not found station to update"}}""".stripMargin
    val deleteStationErrorJson: String = """{"DeleteStationError":{"errorMessage":"Not found station to delete"}}""".stripMargin
    val getStationErrorJson: String = """{"FilterStationError":{"errorMessage":"Not found any station"}}""".stripMargin

    val router: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(CacheStorage())

    val postUri = uri"/api/v1/admin/stations"
    val loginUri = uri"/api/v1/login"
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

    val updateStationMockJson: String = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Rigas 6",
      construction = "outdoor",
      yearOfManufacture = 2008,
      inServiceFrom = 2010,
      name = "as130",
      cityRegion = "Riga",
      latitude = 45.6123,
      longitude = 12.3456,
      zoneOfResponsibility = "Kurzeme"
    ).asJson.noSpaces

    val stationMockJson: String = stationMock.asJson.noSpaces
    val stationsMockJson: String = List(stationMock).asJson.noSpaces

    def check[A](
                  actualResponseIO: IO[Response[IO]],
                  expectedStatus: Status,
                  expectedBody: String,
                  expectedResponseCookie: Option[ResponseCookie] = None
                )(implicit decoder: EntityDecoder[IO, A]): Unit = (
      for {
        actualResponse <- actualResponseIO
        _ <- IO(actualResponse.status shouldBe expectedStatus)
        _ <- actualResponse.as[A].map(_ shouldBe expectedBody)
        _ <- expectedResponseCookie match {
          case Some(responseCookie) => IO(actualResponse.cookies should contain(responseCookie))
          case None => IO.unit
        }
      } yield ()).unsafeRunSync()
  }

  private def login(loginJson: String): Option[ResponseCookie] = {

    val loginUri = uri"/api/v1/login"
    val router: Kleisli[IO, Request[IO], Response[IO]] = StationRoutes.makeRouter(CacheStorage())

    val loginRequest: Request[IO] = Request(
      method = Method.POST,
      uri = loginUri,
      body = Stream.emits(os = loginJson.map(_.toByte))
    )

    router.run(loginRequest).unsafeRunSync().cookies.find(_.name == "authcookie")
  }

}
