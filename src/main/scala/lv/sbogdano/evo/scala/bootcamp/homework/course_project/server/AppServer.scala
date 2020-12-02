package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server

import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.{Config, ServerConfig}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db.{Database, DatabaseStorage}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes.StationRoutes.makeRouter
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object AppServer extends IOApp {

  def server(transactor: Transactor[IO], serverConfig: ServerConfig): IO[ExitCode] = {

    val storage: Storage = new DatabaseStorage(transactor)
    val router = makeRouter(storage)

    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(router)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

//  def init = {
//    for {
//      config     <- Config.of()
//      transactor <- Database.transactor(config.dbConfig)
//      _          <- Database.bootstrap(transactor)
//    } yield server(transactor, config.serverConfig)
//  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config     <- Config.of()
      transactor <- Database.transactor(config.dbConfig)
      _          <- Database.bootstrap(transactor)
      exitCode   <- server(transactor, config.serverConfig)
    } yield exitCode
  }

}
