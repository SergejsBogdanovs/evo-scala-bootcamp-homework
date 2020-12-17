package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.{Blocker, ContextShift, IO, Sync}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.DbConfig
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db.StationQuery.{createTableSchedule, createTableStations, populateSchedule, populateStations}

import scala.concurrent.ExecutionContext

object Database {

  def transactor(dbConfig: DbConfig)(implicit cs: ContextShift[IO]): IO[HikariTransactor[IO]] = {
    val config = new HikariConfig
    config.setJdbcUrl(dbConfig.url)
    config.setUsername(dbConfig.username)
    config.setPassword(dbConfig.password)
    config.setMaximumPoolSize(dbConfig.poolSize)

    val executionContext = ExecutionContext.global
    val blocker = Blocker.liftExecutionContext(executionContext)
    val hikariDataSource = new HikariDataSource(config)
    val transactor = HikariTransactor.apply[IO](hikariDataSource, executionContext, blocker)

    IO(transactor)
  }

  def bootstrap(xa: Transactor[IO]): IO[Unit] = {
    val createTableStationsDDL = Fragment.const(createTableStations)
    val createTableScheduleDDL = Fragment.const(createTableSchedule)
//    val prepopulateStations = Fragment.const(populateStations)
//    val prepopulateSchedule = Fragment.const(populateSchedule)

    val setup = for {
      _ <- createTableStationsDDL.update.run
      _ <- createTableScheduleDDL.update.run
//      _ <- prepopulateStations.update.run
//      _ <- prepopulateSchedule.update.run
    } yield ()

    setup.transact(xa)
  }
}
