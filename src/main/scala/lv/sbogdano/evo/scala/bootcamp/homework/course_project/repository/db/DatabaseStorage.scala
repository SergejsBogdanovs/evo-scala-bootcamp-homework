package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import doobie.implicits._
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, JobEntity, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage


class DatabaseStorage(transactor: Transactor[IO]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, DatabaseCreateStationSuccess]] =
    StationQuery.insertStation(stationEntity).run.transact(transactor).attempt.map {
      case Left(_) => CreateStationError("Error during insertion into Database").asLeft

      case Right(value) =>
        if (value == 1)
          DatabaseCreateStationSuccess(stationEntity).asRight
        else
          CreateStationError("Error during insertion into Database").asLeft
    }

  override def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, DatabaseUpdateStationSuccess]] =
    StationQuery.updateStation(stationEntity).run.transact(transactor).attempt.map {
      case Left(_) => UpdateStationError("Error during update").asLeft

      case Right(value) =>
        if (value == 1)
          DatabaseUpdateStationSuccess(stationEntity).asRight
        else
          UpdateStationError("Error during update").asLeft
    }

  override def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]] =
    StationQuery.searchStationByName(name).to[List].transact(transactor).attempt.map {
      case Left(_)         => FilterStationError("Error during station filtration").asLeft
      case Right(stations) => FilterStationSuccess(stations).asRight
    }

  override def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DatabaseDeleteStationSuccess]] =
    StationQuery.deleteStation(uniqueName).run.transact(transactor).attempt.map {
      case Left(_) => DeleteStationError("Error during delete").asLeft

      case Right(value) =>
        if (value == 1)
          DatabaseDeleteStationSuccess(uniqueName).asRight
        else
          DeleteStationError("Error during delete").asLeft
    }

  // Very very bad code lives here
  override def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] =
    StationQuery.finsJobsByUser(userLogin).transact(transactor).attempt.map {
      case Left(_)         => FindJobsError(s"Can not find any jobs").asLeft
      case Right(userJobs) => if (userJobs.isEmpty) FindJobsError(s"Can not find any jobs").asLeft else UserJobSchedule(userJobs.sorted).asRight
    }.unsafeRunSync()

  // Very very bad code lives here
  def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult] = {
    val jobsWithStations: List[(Job, StationEntity)] = jobSchedule.map(job => (job, job.station))
    val jobs: List[JobEntity] =
      jobsWithStations.map { case (job, _) => job }.map(job => JobEntity(job.id, job.userLogin, job.status.toString, job.priority.toString, job.station.uniqueName))

    val stations: List[StationEntity] = jobsWithStations.map { case (_, stationEntity) => stationEntity }

    StationQuery.insertManyStations(stations).transact(transactor).attempt.map {
            case Left(error)  => UpdateJobError(s"Error during update: $error").asLeft
            case Right(_) =>
              StationQuery.insertManyJobs(jobs).transact(transactor).attempt.map {
                case Left(error)  => UpdateJobError(s"Error during update: $error").asLeft
                case Right(value) => UpdateJobResult(value).asRight
              }.unsafeRunSync()
    }.unsafeRunSync()
  }
}
