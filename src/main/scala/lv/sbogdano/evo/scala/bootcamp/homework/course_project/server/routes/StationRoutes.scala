package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import io.circe.generic.auto._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Auth.{authUser, inAuthFailure}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Role
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}


object StationRoutes {


  /**
   * define following APIS
   *   1. create station — POST api/v1/admin/stations
   *   2. update station — PUT api/v1/admin/stations
   *   3. delete station — DELETE api/v1/admin/stations/{id}
   *   4. search station — GET api/v1/user/station/{name}
   */
  def routes(service: StationService): HttpRoutes[IO] = {
    userRoutes(service) <+> authMiddleware.apply(adminRoutes(service))
  }

  def authMiddleware: AuthMiddleware[IO, Role] = AuthMiddleware(authUser, inAuthFailure)

  def adminRoutes(service: StationService): AuthedRoutes[Role, IO] = {

    AuthedRoutes.of {

      // curl -X POST "localhost:8761/api/v1/admin/stations" -H "Content-Type: application/json" -d '{"uniqueName": "Riga_AS130","stationAddress": "Dammes 6","construction": "construction","yearOfManufacture": 2010,"inServiceFrom": 2011,"name": "as130","cityRegion": "Riga","latitude": 123.45,"longitude": "45.6123","zoneOfResponsibility": "latgale"}'
      case req@POST -> Root / "admin" / "stations" as admin =>
        req.req.as[StationEntity].flatMap { stationEntity =>
          service.createStation(stationEntity).flatMap {
            case Right(stationEntity) => Created(stationEntity)
            case Left(message)        => BadRequest(message)
          }
        }

      //  curl -X PUT "localhost:8761/api/v1/admin/stations" -H "Content-Type: application/json" -d '{"uniqueName": "35016_AS130","address": "Brivibas 5","construction": "construction","yearOfManufacture": "2015","inServiceFrom": "2010","name": "as116","cityRegion": "Dagda","objectType": "A/st","x": "456123","y": "123456","zoneOfResponsibility": "123456"}'
      case req@PUT -> Root / "admin" / "stations" as admin =>
        req.req.as[StationEntity].flatMap { stationEntity =>
          service.updateStation(stationEntity).flatMap {
            case Right(value)  => Ok(value)
            case Left(message) => NotFound(message)
          }
        }

      // curl -X DELETE "localhost:8761/api/v1/admin/stations/uniqueName
      case DELETE -> Root / "admin" / "stations" / uniqueName as admin =>
        service.deleteStation(uniqueName).flatMap {
          case Right(value)  => Ok(value)
          case Left(message) => NotFound(message)
        }
    }
  }

  def userRoutes(service: StationService): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {

      // curl localhost:8761/api/v1/user/stations/as130
      case GET -> Root / "user" / "stations" / name =>
        service.filterStations(name).flatMap {
          case Right(value)  => Ok(value)
          case Left(message) => NotFound(message)
        }
    }
  }

  def makeRouter(storage: Storage): Kleisli[IO, Request[IO], Response[IO]] = {
    val service = StationService(storage)
    Router[IO]("api/v1" -> routes(service)).orNotFound
  }
}
