package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits.{catsSyntaxEitherId, toSemigroupKOps}
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import io.circe.generic.auto._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Auth.{authUser, inAuthFailure, loginUser}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.{Role, User}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Role.{Admin, Worker}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.WorkerJobsState
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.InputMessage.{Disconnect, EnterJobSchedule}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}


object StationRoutes {


  /**
   * define following APIS
   *   1. create station — POST api/v1/admin/stations
   *      2. update station — PUT api/v1/admin/stations
   *      3. delete station — DELETE api/v1/admin/stations/{id}
   *      4. search station — GET api/v1/user/station/{name}
   */
  def routes(
              service: StationService,
              jobsScheduleState: Ref[IO, WorkerJobsState],
              queue: Queue[IO, InputMessage],
              topic: Topic[IO, OutputMessage]
            ): HttpRoutes[IO] = {


    loginRoutes <+>
      wsRouter(jobsScheduleState, queue, topic) <+>
        authMiddleware.apply(adminRoutes(service))
  }

  def authMiddleware: AuthMiddleware[IO, Role] = AuthMiddleware(authUser, inAuthFailure)
  //def loginMiddleware: AuthMiddleware[IO, User] = AuthMiddleware(loginUser, inAuthFailure)

  def adminRoutes(service: StationService): AuthedRoutes[Role, IO] = {

    AuthedRoutes.of {


      // curl -X POST "localhost:8761/api/v1/admin/stations" -H "Content-Type: application/json" -d '{"uniqueName": "Riga_AS130","stationAddress": "Dammes 6","construction": "construction","yearOfManufacture": 2010,"inServiceFrom": 2011,"name": "as130","cityRegion": "Riga","latitude": 123.45,"longitude": "45.6123","zoneOfResponsibility": "latgale"}'
      case req@POST -> Root / "admin" / "stations" as role =>
        role match {
          case Worker => IO(Response(Unauthorized))

          case Admin =>
            req.req.as[StationEntity].flatMap { stationEntity =>
              service.createStation(stationEntity).flatMap {
                case Right(stationEntity) => Created(stationEntity)
                case Left(message) => BadRequest(message)
              }
            }
        }

      //  curl -X PUT "localhost:8761/api/v1/admin/stations" -H "Content-Type: application/json" -d '{"uniqueName": "35016_AS130","address": "Brivibas 5","construction": "construction","yearOfManufacture": "2015","inServiceFrom": "2010","name": "as116","cityRegion": "Dagda","objectType": "A/st","x": "456123","y": "123456","zoneOfResponsibility": "123456"}'
      case req@PUT -> Root / "admin" / "stations" as role =>
        role match {
          case Worker => IO(Response(Unauthorized))

          case Admin =>
            req.req.as[StationEntity].flatMap { stationEntity =>
              service.updateStation(stationEntity).flatMap {
                case Right(stationEntity) => Ok(stationEntity)
                case Left(message) => NotFound(message)
              }
            }
        }

      // curl -X DELETE "localhost:8761/api/v1/admin/stations/uniqueName
      case DELETE -> Root / "admin" / "stations" / uniqueName as role =>
        role match {
          case Worker => IO(Response(Unauthorized))

          case Admin =>
            service.deleteStation(uniqueName).flatMap {
              case Right(uniqueName) => Ok(uniqueName)
              case Left(message) => NotFound(message)
            }
        }

      // curl localhost:8761/api/v1/user/stations/as130
      case GET -> Root / "user" / "stations" / name as role =>
        service.filterStations(name).flatMap {
          case Right(stationEntities) => Ok(stationEntities)
          case Left(message) => NotFound(message)
        }
    }
  }


  def loginRoutes: HttpRoutes[IO] = {
    HttpRoutes.of {

      // curl -XPOST "localhost:8761/api/v1/login json" -d '{"name":"worker", "password":"worker", "role":"worker"}' -H "Content-Type: application/json"
      case req @ POST -> Root / "login" =>
        loginUser(req)
    }
  }

  def wsRouter(
                jobsScheduleState: Ref[IO, WorkerJobsState],
                queue: Queue[IO, InputMessage],
                topic: Topic[IO, OutputMessage]
              ): HttpRoutes[IO] = {

    HttpRoutes.of {

      case GET -> Root / "ws" / workerName =>

        // WebSocket Output messages
        // Routes messages from "topic" to WebSocket
        // Messages which returns to client
        val toClient: Stream[IO, WebSocketFrame.Text] =
          topic
            .subscribe(1000)
            .filter(_.forWorker(workerName))
            .map(msg => Text(msg.toString))


        //WebSocket Input messages
        def processInput(webSocketStream: Stream[IO, WebSocketFrame]): Stream[IO, Unit] = {

          val entryStream: Stream[IO, InputMessage] = Stream.emits(Seq(EnterJobSchedule(workerName, InputMessage.emptyJobSchedule)))

          val parsedWebSocketInput: Stream[IO, InputMessage] =
            webSocketStream.collect {
              case Text(text, _) => InputMessage.parse(workerName, text)
              case Close(_) => Disconnect(workerName)
            }

          // Enqueue messages to Queue for processing
          (entryStream ++ parsedWebSocketInput).through(queue.enqueue)
        }

        val inputPipe: Pipe[IO, WebSocketFrame, Unit] = processInput

        WebSocketBuilder[IO].build(toClient, inputPipe)
    }
  }

  def makeRouter(
                  storage: Storage,
                  jobsScheduleState: Ref[IO, WorkerJobsState] = null,
                  queue: Queue[IO, InputMessage] = null,
                  topic: Topic[IO, OutputMessage] = null
                ): Kleisli[IO, Request[IO], Response[IO]] = {
    val service = StationService(storage)
    Router[IO]("api/v1" -> routes(service, jobsScheduleState, queue, topic)).orNotFound
  }
}
