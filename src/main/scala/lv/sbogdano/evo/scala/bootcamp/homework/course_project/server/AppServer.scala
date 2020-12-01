package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.{Config, ServerConfig}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db.{Database, DatabaseStorage}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes.StationRoutes
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}

import scala.concurrent.ExecutionContext

object AppServer extends IOApp {

  def makeRouter(transactor: Transactor[IO]): Kleisli[IO, Request[IO], Response[IO]] = {
    val storage = new DatabaseStorage(transactor)
    val service = StationService(storage)
    Router[IO]("api/v1" -> StationRoutes.routes(service)).orNotFound
  }

  def server(transactor: Transactor[IO], serverConfig: ServerConfig): IO[ExitCode] = {
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(makeRouter(transactor))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config     <- Config.of()
      transactor <- Database.transactor(config.dbConfig)
      _          <- Database.bootstrap(transactor)
      exitCode   <- server(transactor, config.serverConfig)
    } yield exitCode
  }

}
