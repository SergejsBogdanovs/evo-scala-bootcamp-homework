package lv.sbogdano.evo.scala.bootcamp.homework.course_project.http.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits.toSemigroupKOps
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.auth.{Admin, Role, Worker}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository.{CacheCreateStationSuccess, CacheDeleteStationSuccess, CacheUpdateStationSuccess, CreateStationError, DeleteStationError, FilterStationError, FilterStationSuccess, RepositoryError, RepositoryResponse, UpdateStationError}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.Job
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.{InputMessage, OutputMessage}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.http.auth.Auth.{authUser, inAuthFailure, loginUser}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}

import java.time.Instant


object StationRoutes {

  /**
   * define following APIS
   *   1. create station — POST api/v1/admin/stations
   *   2. update station — PUT api/v1/admin/stations
   *   3. delete station — DELETE api/v1/admin/stations/{id}
   *   4. search station — GET api/v1/user/station/{name}
   */
  def routes(
              serviceRef: Ref[IO, StationService],
              queue: Queue[IO, InputMessage],
              topic: Topic[IO, OutputMessage]
            ): HttpRoutes[IO] = {

    loginRoutes <+>
      wsRouter(serviceRef, queue, topic) <+>
        authMiddleware.apply(authedRoutes(serviceRef))
  }

  def authMiddleware: AuthMiddleware[IO, Role] = AuthMiddleware(authUser, inAuthFailure)

  def authedRoutes(
                    serviceRef: Ref[IO, StationService],
                  ): AuthedRoutes[Role, IO] = {

    AuthedRoutes.of {

      case req@POST -> Root / "admin" / "stations" as role =>
        role match {
          case Worker => IO(Response(Unauthorized))

          case Admin =>
            req.req.as[StationEntity].flatMap { stationEntity =>
              serviceRef.modify(_.createStation(stationEntity)).flatMap {
                case error: CreateStationError         => BadRequest(error.asJson)
                case result: CacheCreateStationSuccess => Created(result.asJson)
              }
            }
        }

      case req@PUT -> Root / "admin" / "stations" as role =>
        role match {
          case Worker => IO(Response(Unauthorized))
          case Admin =>
            req.req.as[StationEntity].flatMap { stationEntity =>
              serviceRef.modify(_.updateStation(stationEntity)).flatMap {
                case error: UpdateStationError         => NotFound(error.asJson)
                case result: CacheUpdateStationSuccess => Ok(result.asJson)
              }
            }
        }

      case DELETE -> Root / "admin" / "stations" / uniqueName as role =>
        role match {
          case Worker => IO(Response(Unauthorized))

          case Admin =>
            serviceRef.modify(_.deleteStation(uniqueName)) flatMap {
              case error: DeleteStationError         => NotFound(error.asJson)
              case result: CacheDeleteStationSuccess => Ok(result.asJson)
            }
        }

      case GET -> Root / "user" / "stations" / name as role =>

        serviceRef.modify(_.filterStations(name)) flatMap {
          case error: FilterStationError    => NotFound(error.asJson)
          case result: FilterStationSuccess => Ok(result.asJson)
        }
    }
  }

  def loginRoutes: HttpRoutes[IO] = {
    HttpRoutes.of {
      case req @ POST -> Root / "login" =>
        loginUser(req)
    }
  }

  def wsRouter(
                serviceRef: Ref[IO, StationService],
                queue: Queue[IO, InputMessage],
                topic: Topic[IO, OutputMessage]
              ): HttpRoutes[IO] = {

    HttpRoutes.of {

      case req@POST -> Root / "schedule" =>
        req.as[Job].flatMap { job =>
          serviceRef.modify(_.addJobToSchedule(job)) flatMap {
            case h :: Nil => h.outputAction match {
              case UserJobSchedule(jobSchedule) => Created(jobSchedule)
              case AddJobError(error)           => NotFound(error)
            }
            case Nil      => NotFound()
          }
        }

      case GET -> Root / "ws" / userLogin =>

          // WebSocket Output messages
          // Routes messages from "topic" to WebSocket
          // Messages which returns to client
          val toClient: Stream[IO, WebSocketFrame.Text] =
            topic
              .subscribe(1000)
              .filter(_.forUser(userLogin))
              .map(outputMessage => Text(outputMessage.toString))


          //WebSocket Input messages
          def processInput(webSocketStream: Stream[IO, WebSocketFrame]): Stream[IO, Unit] = {

            val entryStream: Stream[IO, InputMessage] =
              Stream.emits(Seq(InputMessage.from(userLogin, UserAction(Instant.now(), EnterJobSchedule).asJson.noSpaces)))

            val parsedWebSocketInput: Stream[IO, InputMessage] =
              webSocketStream.collect {
                case Text(text, _) => InputMessage.from(userLogin, text)
                case Close(_)      => InputMessage(userLogin, DisconnectUser)
              }

            // Enqueue messages to Queue for processing
            (entryStream ++ parsedWebSocketInput).through(queue.enqueue)
          }

          val inputPipe: Pipe[IO, WebSocketFrame, Unit] = processInput

          WebSocketBuilder[IO].build(toClient, inputPipe)
        }


    }

  def makeRouter(
                  serviceRef: Ref[IO, StationService],
                  queue: Queue[IO, InputMessage],
                  topic: Topic[IO, OutputMessage]
                ): Kleisli[IO, Request[IO], Response[IO]] = Router[IO]("api/v1" -> routes(serviceRef, queue, topic)).orNotFound

}
