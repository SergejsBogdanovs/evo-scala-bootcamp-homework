package lv.sbogdano.evo.scala.bootcamp.homework.course_project.http.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.Stream
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.auth.{AuthResponseError, AuthResponseSuccess, User}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, JobsState, Normal, Pending}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Set-Cookie`
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StationRoutesSpec extends AnyFlatSpec with Matchers {

  "HttpService" should "receive AuthResponseError, when try login as invalid user" in new Scope {

    val request: Request[IO] = Request(
      method = Method.POST,
      uri = loginUri,
      body = Stream.emits(os = loginInvalidUser.asJson.noSpaces.map(_.toByte))
    )

    val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Forbidden,
      expectedBody = authInvalidUserJson.noSpaces,
    )
  }

  // UNAUTHORIZED CREATE
  "HttpService" should "receive Status 401 Unauthorized error when unauthorized user try CREATE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = postUri,
    )

    val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Forbidden,
      expectedBody = authCookieNotFoundJson.noSpaces,
    )
  }

  // UNAUTHORIZED UPDATE
  it should "receive Status 401 Unauthorized error when unauthorized user try UPDATE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.PUT,
      uri = postUri,
    )

    val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Forbidden,
      expectedBody = authCookieNotFoundJson.noSpaces
    )
  }

  // UNAUTHORIZED DELETE
  it should "receive Status 401 Unauthorized error when unauthorized user try DELETE Station" in new Scope {
    val request: Request[IO] = Request(
      method = Method.DELETE,
      uri = postUri,
    )

    val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Forbidden,
      expectedBody = authCookieNotFoundJson.noSpaces
    )
  }

  // UNAUTHORIZED GET
  it should "receive Status 401 Unauthorized when unauthorized user GET Stations" in new Scope {

    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    val getRequest: Request[IO] = Request(
      method = Method.GET,
      uri = getUri,
    )

    val response: IO[Response[IO]] = routerGet.run(getRequest)
    check[String](
      actualResponseIO = response,
      expectedStatus = Forbidden,
      expectedBody = authCookieNotFoundJson.noSpaces
    )
  }

  // AUTHORIZED CREATE as ADMIN
  it should "receive Status 201 Created and StationBody as body when ADMIN created Station successfully" in new Scope {

    login(loginAdmin) match {
      case Some(responseCookie) => {
        val request: Request[IO] = Request(
          method = Method.POST,
          uri = postUri,
          body = Stream.emits(os = stationMockJson.noSpaces.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Created,
          expectedBody = createStationResponseJson.noSpaces,
        )
      }
      case None => fail("Can not find authcookie")
    }
  }

  // AUTHORIZED CREATE as WORKER
  it should "receive Status 401 Unauthorized when Worker try CREATE Station" in new Scope {

    login(loginWorker) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.POST,
          uri = postUri,
          body = Stream.emits(os = stationMockJson.noSpaces.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
        )
      case None => fail("Can not find authcookie")
    }
  }

  // INVALID UPDATE. ADMIN
  it should "receive Status 404 Not found when authorized ADMIN try to UPDATE Station which is not in Storage" in new Scope {

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.noSpaces.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = updateStationErrorJson.noSpaces
        )
      case None => fail("Can not find authcookie")
    }
  }

  // VALID UPDATE. ADMIN
  it should "receive Status 200 Ok and updated StationEntity when ADMIN UPDATE Station successfully" in new Scope {

    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val updateRequest: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.noSpaces.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerUpdate.run(updateRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = updateStationResponseJson.noSpaces
        )

      case None => fail("Can not find authcookie")
    }

  }

  // INVALID UPDATE. WORKER
  it should "receive Status 401 Unauthorized when WORKER try UPDATE Station" in new Scope {

    val routerUpdate: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()


    login(loginWorker) match {
      case Some(responseCookie) =>
        val updateRequest: Request[IO] = Request(
          method = Method.PUT,
          uri = postUri,
          body = Stream.emits(os = updateStationMockJson.noSpaces.map(_.toByte)),
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerUpdate.run(updateRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
        )

      case None => fail("Can not find authcookie")
    }
  }

  // AUTHORIZED GET. ADMIN
  it should "receive Status 200 Ok and List[StationEntity] when ADMIN GET Stations" in new Scope {

    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerGet.run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = getStationsResponseJson.noSpaces
        )
      case None => fail("Can not find authcookie")
    }
  }

  // AUTHORIZED GET. WORKER
  it should "receive Status 200 Ok and List[StationEntity] when authorized WORKER GET Stations" in new Scope {

    val routerGet: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    login(loginWorker) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerGet.run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = getStationsResponseJson.noSpaces
        )

      case None => fail("Can not find authcookie")
    }
  }

  // INVALID GET. ADMIN
  it should "receive Status 404 Not found when ADMIN try get Stations which are not in Storage" in new Scope {

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val getRequest: Request[IO] = Request(
          method = Method.GET,
          uri = getUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.unsafeRunSync().run(getRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = getStationErrorJson.noSpaces
        )
      case None => fail("Can not find authcookie")
    }
  }

  // INVALID DELETE
  it should "receive Status 404 Not Found when ADMIN try delete Station which is not in Storage" in new Scope {

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val request: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.NotFound,
          expectedBody = deleteStationErrorJson.noSpaces
        )
      case None => fail("Can not find authcookie")
    }
  }

  // VALID DELETE. ADMIN
  it should "receive Status 200 Ok and deleted StationEntity uniqueName when ADMIN delete Station successfully" in new Scope {

    val routerCreate: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    login(loginAdmin) match {
      case Some(responseCookie) =>
        val deleteRequest: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerCreate.run(deleteRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Ok,
          expectedBody = deletedStationResponseJson.noSpaces
        )
      case None => fail("Can not find authcookie")
    }
  }

  // INVALID DELETE. WORKER
  it should "receive Status 401 Unauthorized when WORKER try DELETE Station" in new Scope {

    val routerCreate: Kleisli[IO, Request[IO], Response[IO]] = prepopulateWithData.unsafeRunSync()

    login(loginWorker) match {
      case Some(responseCookie) =>
        val deleteRequest: Request[IO] = Request(
          method = Method.DELETE,
          uri = deleteUri,
          headers = Headers.of(`Set-Cookie`(responseCookie))
        )

        val response: IO[Response[IO]] = routerCreate.run(deleteRequest)
        check[String](
          actualResponseIO = response,
          expectedStatus = Status.Unauthorized,
        )
      case None => fail("Can not find authcookie")
    }
  }

  // CREATE JOB
  it should "receive Status 201 Created and JobSchedule as body when Job created successfully" in new Scope {
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = addJobUri,
      body = Stream.emits(os = job.asJson.noSpaces.map(_.toByte))
    )

    val response: IO[Response[IO]] = router.unsafeRunSync().run(request)
    check[String](
      actualResponseIO = response,
      expectedStatus = Created,
      expectedBody = jobScheduleJson.noSpaces,
    )
  }

  trait Scope {

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

    val updateStationMock: StationEntity = StationEntity(
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
    )

    val job: Job = Job(
      id = 1,
      userLogin = "login",
      status = Pending,
      priority = Normal(),
      station = stationMock
    )

    val stationMockJson: Json = stationMock.asJson
    val updateStationMockJson: Json = updateStationMock.asJson
    val jobScheduleJson: Json = List(job).asJson

    val loginAdmin: User = User(login = "admin", password = "admin")
    val loginWorker: User = User(login = "worker", password = "worker")
    val loginInvalidUser: User = User(login = "invalid", password = "invalid")

    val authResponseSuccessJson: Json = AuthResponseSuccess("Logged in").asJson
    val authCookieNotFoundJson: Json = AuthResponseError("Couldn't find the user by given token").asJson
    val authUserNotFoundJson: Json = AuthResponseError("Couldn't find user by provided cookies").asJson
    val authInvalidUserJson: Json = AuthResponseError("Invalid user").asJson

    val deletedStationResponseJson: Json = CacheDeleteStationSuccess(List.empty).asJson
    val getStationsResponseJson: Json = FilterStationSuccess(List(stationMock)).asJson
    val updateStationResponseJson: Json = CacheUpdateStationSuccess(List(updateStationMock)).asJson
    val createStationResponseJson: Json = CacheCreateStationSuccess(List(stationMock)).asJson

    val updateStationErrorJson: Json = UpdateStationError("Not found station to update").asJson
    val deleteStationErrorJson: Json = DeleteStationError("Not found station to delete").asJson
    val getStationErrorJson: Json = FilterStationError("Not found any station").asJson

    val router: IO[Kleisli[IO, Request[IO], Response[IO]]] = for {
        ref    <- Ref.of[IO, StationService](StationService(JobsState(), null))
        router = StationRoutes.makeRouter(serviceRef = ref, queue = null, topic = null)
    } yield router

    val postUri = uri"/api/v1/admin/stations"
    val loginUri = uri"/api/v1/login"
    val deleteUri = uri"/api/v1/admin/stations/Riga_AS130"
    val getUri = uri"/api/v1/user/stations/as130"
    val addJobUri = uri"/api/v1/schedule"


    def check[A](
                  actualResponseIO: IO[Response[IO]],
                  expectedStatus: Status,
                  expectedBody: String = "",
                  expectedResponseCookie: Option[ResponseCookie] = None
                )(implicit decoder: EntityDecoder[IO, A]): Unit = (
      for {
        actualResponse <- actualResponseIO
        _ <- IO(actualResponse.status shouldBe expectedStatus)
        _ <- actualResponse.as[A].map(m => m shouldBe expectedBody)
        _ <- expectedResponseCookie match {
          case Some(responseCookie) => IO(actualResponse.cookies should contain(responseCookie))
          case None                 => IO(actualResponse.cookies shouldBe List.empty)
        }
      } yield ()).unsafeRunSync()
  }

  private def login(user: User): Option[ResponseCookie] = {

    val loginUri = uri"/api/v1/login"
    val router = for {
      ref <- Ref.of[IO, StationService](StationService(JobsState(), null))
      router = StationRoutes.makeRouter(serviceRef = ref, queue = null, topic = null)
      } yield router


    val loginRequest: Request[IO] = Request(
      method = Method.POST,
      uri = loginUri,
      body = Stream.emits(os = user.asJson.noSpaces.map(_.toByte))
    )

    router.unsafeRunSync().run(loginRequest).unsafeRunSync().cookies.find(_.name == "authcookie")
  }

  private def prepopulateWithData = {

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

    for {
      ref    <- Ref.of[IO, StationService](StationService(JobsState(CacheStorage(null, List(stationMock))), null))
      router = StationRoutes.makeRouter(serviceRef = ref, queue = null, topic = null)
    } yield router
  }

}
