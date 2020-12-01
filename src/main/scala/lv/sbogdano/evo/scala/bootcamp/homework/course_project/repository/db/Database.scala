package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.{Blocker, ContextShift, IO, Sync}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.DbConfig
import doobie.implicits._

import scala.concurrent.ExecutionContext

object Database {

  def transactor(dbConfig: DbConfig)(implicit cs: ContextShift[IO], F: Sync[IO]): IO[HikariTransactor[IO]] = {
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

  def bootstrap(xa: Transactor[IO]): IO[Int] = StationQuery.createTable.run.transact(xa)
}
