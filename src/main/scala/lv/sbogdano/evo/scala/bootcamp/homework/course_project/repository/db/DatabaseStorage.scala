package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import doobie.implicits._
import doobie.util.transactor.Transactor
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, JobEntity, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{FindJobsError, OutputActionError, UpdateJobError, UpdateJobResult, UserJobSchedule}


class DatabaseStorage(transactor: Transactor[IO]) extends Storage {

  override def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]] =
    StationQuery.insertStation(stationEntity).run.transact(transactor).attempt.map {
      case Left(_) => CreateStationError("Error during insertion into Database").asLeft

      case Right(value) =>
        if (value == 1)
          CreateStationSuccess(stationEntity).asRight
        else
          CreateStationError("Error during insertion into Database").asLeft
    }

  override def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, UpdateStationSuccess]] =
    StationQuery.updateStation(stationEntity).run.transact(transactor).attempt.map {
      case Left(_) => UpdateStationError("Error during update").asLeft

      case Right(value) =>
        if (value == 1)
          UpdateStationSuccess(stationEntity).asRight
        else
          UpdateStationError("Error during update").asLeft
    }

  override def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]] =
    StationQuery.searchStationByName(name).to[List].transact(transactor).attempt.map {
      case Left(_)         => FilterStationError("Error during station filtration").asLeft
      case Right(stations) => FilterStationSuccess(stations).asRight
    }

  override def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DeleteStationSuccess]] =
    StationQuery.deleteStation(uniqueName).run.transact(transactor).attempt.map {
      case Left(_) => DeleteStationError("Error during delete").asLeft

      case Right(value) =>
        if (value == 1)
          DeleteStationSuccess(uniqueName).asRight
        else
          DeleteStationError("Error during delete").asLeft
    }


  override def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = {
    StationQuery.finsJobsByUser(userLogin).transact(transactor).attempt.map {
      case Left(error)     => FindJobsError(s"Can not find any jobs: $error").asLeft
      case Right(userJobs) => UserJobSchedule(userJobs).asRight
    }.unsafeRunSync()
  }

  // Very bad code :)
  def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult] = {
    val t: List[(Job, StationEntity)] = jobSchedule.map(job => (job, job.station))
    val jobs: List[JobEntity] =
      t.map { case (job, _) => job }.map(job => JobEntity(job.id, job.userLogin, job.status.toString, job.priority.toString, job.station.uniqueName))

    val stations: List[StationEntity] = t.map { case (_, stationEntity) => stationEntity }

    StationQuery.insertManyStations(stations).transact(transactor).attempt.map {
            case Left(error)  => UpdateJobError(s"Error during update: $error").asLeft
            case Right(_) =>
              StationQuery.insertManyJobs(jobs).transact(transactor).attempt.map {
                case Left(error)  => UpdateJobError(s"Error during update: $error").asLeft
                case Right(value) => UpdateJobResult(value).asRight
              }.unsafeRunSync()
    }.unsafeRunSync()
  }

  override def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = ???

  override def updateJobPriority(userLogin: UserLogin, jobId: Int, priority: Priority): Either[OutputActionError, UserJobSchedule] = ???

  override def updateJobStatus(userLogin: UserLogin, jobId: Int, status: Status): Either[OutputActionError, UserJobSchedule] = ???

  override def addJobToSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = ???

  override def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = ???
}
