package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import doobie.implicits._
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{OutputActionError, UserJobSchedule}


class DatabaseStorage(transactor: Transactor[IO]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]] =
    for {
      insert <- StationQuery.insertStation(stationEntity).run.transact(transactor)
      result <- IO {
        if (insert == 1)
          CreateStationSuccess(stationEntity).asRight
        else
          CreateStationError("Error during insertion into Database").asLeft
      }
    } yield result

  override def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, UpdateStationSuccess]] =
    for {
      update <- StationQuery.updateStation(stationEntity).run.transact(transactor)
      result <- IO {
        if (update == 1)
          UpdateStationSuccess(stationEntity).asRight
        else
          UpdateStationError("Error during update").asLeft
      }
    } yield result

  override def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]] =
    StationQuery.searchStationByName(name).to[List].transact(transactor).attempt.map {
      case Left(_)         => FilterStationError("Error during station filtration").asLeft
      case Right(stations) => FilterStationSuccess(stations).asRight
    }

  override def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DeleteStationSuccess]] =
    for {
      delete <- StationQuery.deleteStation(uniqueName).run.transact(transactor)
      result <- IO {
        if (delete == 1)
          DeleteStationSuccess(uniqueName).asRight
        else
          DeleteStationError("Error during delete").asLeft
      }
    } yield result




  override def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = ???

  override def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = ???

  override def updateJobPriority(userLogin: UserLogin, jobId: Int, priority: Priority): Either[OutputActionError, UserJobSchedule] = ???

  override def updateJobStatus(userLogin: UserLogin, jobId: Int, status: Status): Either[OutputActionError, UserJobSchedule] = ???

  override def addJobToSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = ???

  override def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = ???
}