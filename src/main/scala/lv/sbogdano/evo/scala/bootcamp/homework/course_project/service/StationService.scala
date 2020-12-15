package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import cats.effect.concurrent.Ref
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOps._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, JobsState, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{AddJobError, AddJobToSchedule, DeleteJobError, DeleteJobFromSchedule, Disconnect, EnterJobSchedule, FindJobsByUser, FindJobsByUserAndStatus, FindJobsError, InvalidInput, InvalidInputError, OutputActionError, UpdateJobError, UpdateJobPriority, UpdateJobStatus, UserAction, UserJobSchedule, WelcomeUser}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage}

import java.time.Instant

class StationService(jobState: JobsState, storage: Storage) {


  def createStation(stationEntity: StationEntity): IO[Either[CreateStationError, CreateStationSuccess]] =
    storage.createStation(stationEntity)

  def updateStation(stationEntity: StationEntity): IO[Either[UpdateStationError, UpdateStationSuccess]] =
    storage.updateStation(stationEntity)

  def filterStations(name: String): IO[Either[FilterStationError, FilterStationSuccess]] =
    storage.filterStations(name)

  def deleteStation(uniqueName: String): IO[Either[DeleteStationError, DeleteStationSuccess]] =
    storage.deleteStation(uniqueName)


  // JobsSchedule

  def process(msg: InputMessage): (StationService, Seq[OutputMessage]) = {
    val (state, outputMessages) = jobState.process(msg)
    outputMessages match {
      case h :: Nil => h.outputAction match {

        case WelcomeUser(_) | UserJobSchedule(_) => (StationService(state, storage), outputMessages)

        case error: OutputActionError => error match {

          case FindJobsError(_) =>

            findJobsByUser(msg.userLogin) match {
              case Left(error) =>
                val seq = Seq(OutputMessage(msg.userLogin, error))
                (StationService(state, storage), seq)

              case Right(listUseJobs) =>
                val seq = Seq(OutputMessage(msg.userLogin, listUseJobs))
                (StationService(state, storage), seq)
            }

          case UpdateJobError(_) | AddJobError(_) | DeleteJobError(_) | InvalidInputError(_) =>
            (StationService(state, storage), outputMessages)
        }
      }
    }
  }

  def addJobToSchedule(job: Job): (StationService, Seq[OutputMessage]) = {
    process(InputMessage.from("admin", UserAction(Instant.now(), AddJobToSchedule(job)).asJson.noSpaces))
  }

  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] =
    storage.findJobsByUser(userLogin)

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] =
    storage.findJobsByUserAndStatus(userLogin, status)

  def updateJobStatus(userLogin: UserLogin, jobId: Int, status: Status): Either[OutputActionError, UserJobSchedule] =
    storage.updateJobStatus(userLogin, jobId, status)

  def updateJobPriority(userLogin: UserLogin, jobId: Int, priority: Priority): Either[OutputActionError, UserJobSchedule] =
    storage.updateJobPriority(userLogin, jobId, priority)

  def deleteJobFroSchedule(job: Job): Either[OutputActionError, UserJobSchedule] =
    storage.deleteJobFromSchedule(job)
}

object StationService {
  def apply(jobState: JobsState, storage: Storage) = new StationService(jobState, storage)
}
