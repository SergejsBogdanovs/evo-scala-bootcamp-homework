package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository.{CacheCreateStationSuccess, CacheDeleteStationSuccess, CacheUpdateStationSuccess, CreateStationError, DeleteStationError, FilterStationError, FilterStationSuccess, UpdateStationError}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

object JobsState {
  def apply(): JobsState = JobsState(CacheStorage())
  type JobSchedule = List[Job]
  type UserLogin = String
}

case class JobsState(cacheStorage: CacheStorage) {

  def createStation(stationEntity: StationEntity): Either[CreateStationError, (JobsState, CacheCreateStationSuccess)] = {
    cacheStorage.createStation(stationEntity) match {
      case Left(error)  => error.asLeft
      case Right(success) => (JobsState(CacheStorage(stations = success.stationEntities)), success).asRight
    }
  }

  def updateStation(stationEntity: StationEntity): Either[UpdateStationError, (JobsState, CacheUpdateStationSuccess)] = {
    cacheStorage.updateStation(stationEntity) match {
      case Left(error)  => error.asLeft
      case Right(success) => (JobsState(CacheStorage(stations = success.stationEntities)), success).asRight
    }
  }

  def deleteStation(uniqueName: String): Either[DeleteStationError, (JobsState, CacheDeleteStationSuccess)] = {
    cacheStorage.deleteStation(uniqueName) match {
      case Left(error)  => error.asLeft
      case Right(success) => (JobsState(CacheStorage(stations = success.stationEntities)), success).asRight
    }
  }

  def filterStations(name: String): Either[FilterStationError, (JobsState, FilterStationSuccess)] = {
    cacheStorage.filterStations(name) match {
      case Left(error)    => error.asLeft
      case Right(success) => (this, success).asRight
    }
  }

  def process(msg: InputMessage): (JobsState, Seq[OutputMessage]) = msg.action match {

    case EnterJobSchedule =>
      cacheStorage.findJobsByUser(msg.userLogin) match {
        case Left(_) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = List.empty,
            outputAction = WelcomeUser(s"Welcome, ${msg.userLogin.capitalize}! Today is another great day for work.")
          )
        case Right(_) =>(this, Seq(OutputMessage(msg.userLogin, WelcomeUser(s"Welcome, ${msg.userLogin.capitalize}! Today is another great day for work."))))
      }

    case FindJobsByUser =>
      cacheStorage.findJobsByUser(msg.userLogin) match {
        case Left(errorOutputAction) => (this, Seq(OutputMessage(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(OutputMessage(msg.userLogin, listUserJobs)))
      }

    case FindJobsByUserAndStatus(status) =>
      cacheStorage.findJobsByUserAndStatus(msg.userLogin, status) match {
        case Left(errorOutputAction) => (this, Seq(OutputMessage(msg.userLogin, errorOutputAction)))
        case Right(listUserJobs)     => (this, Seq(OutputMessage(msg.userLogin, listUserJobs)))
      }

    case AddJobToSchedule(job) =>
      cacheStorage.addJobToSchedule(job) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobStatus(jobId, status) =>
      cacheStorage.updateJobStatus(msg.userLogin, jobId, status) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case UpdateJobPriority(jobId, priority) =>
      cacheStorage.updateJobPriority(msg.userLogin, jobId, priority) match {
        case Left(error)         => (this, Seq(OutputMessage(msg.userLogin, error)))
        case Right(outputAction) =>
          updateState(
            userLogin = msg.userLogin,
            jobSchedule = outputAction.jobSchedule,
            outputAction = outputAction
          )
      }

    case DeleteJobFromSchedule(job) =>
      cacheStorage.updateJobStatus(job.userLogin, job.id, newStatus = Rejected) match {
        case Left(_)         => (this, Seq(OutputMessage(msg.userLogin, DeleteJobError("Couldn't find job to delete"))))
        case Right(outputAction) => updateState(
          userLogin = msg.userLogin,
          jobSchedule = outputAction.jobSchedule,
          outputAction = outputAction
        )
      }

    case InvalidInput =>
      (this, Seq(OutputMessage(msg.userLogin, InvalidInputError("Invalid input"))))

    case DisconnectUser =>
      (this, Seq(OutputMessage(msg.userLogin, DisconnectResult("User disconnected"))))
  }

  def updateState(userLogin: UserLogin, jobSchedule: JobSchedule, outputAction: OutputAction): (JobsState, Seq[OutputMessage]) = {
    val nextState = JobsState(CacheStorage(jobSchedule))
    (nextState, Seq(OutputMessage(userLogin, outputAction)))
  }
}

