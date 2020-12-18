package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.repository.RepositoryOps.{CreateStationError, CreateStationSuccess, DeleteStationError, DeleteStationSuccess, FilterStationError, FilterStationSuccess, UpdateStationError, UpdateStationSuccess}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.{JobSchedule, UserLogin}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, JobsState}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.{InputMessage, OutputMessage}

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

        case WelcomeUser(_) | UserJobSchedule(_) | UpdateJobResult(_) => (StationService(state, storage), outputMessages)

        case DisconnectResult(_) =>
          val cachedJobSchedule = jobState.cacheStorage.getJobSchedule
          updateDatabaseWithCache(cachedJobSchedule) match {
            case Left(error) =>
              val seq = Seq(OutputMessage(msg.userLogin, error))
              (StationService(state, storage), seq)

            case Right(value) =>
              val seq = Seq(OutputMessage(msg.userLogin, value))
              (StationService(state, storage), seq)
          }


        case error: OutputActionError => error match {

          case FindJobsError(_) =>

            getJobsFromDatabase(msg.userLogin) match {
              case Left(error) =>
                val seq = Seq(OutputMessage(msg.userLogin, error))
                (StationService(state, storage), seq)

              case Right(userJobSchedule) =>
                val (updatedState, outputMessages) = jobState.updateState(msg.userLogin, userJobSchedule.jobSchedule, userJobSchedule)
                (StationService(updatedState, storage), outputMessages)
            }

          case UpdateJobError(_) | AddJobError(_) | DeleteJobError(_) | InvalidInputError(_) | SystemError(_) =>
            (StationService(state, storage), outputMessages)
        }
      }

      case Nil => (StationService(state, storage), Seq(OutputMessage(msg.userLogin, SystemError("System error"))))
    }
  }

  def addJobToSchedule(job: Job): (StationService, Seq[OutputMessage]) =
    process(InputMessage.from("admin", UserAction(Instant.now(), AddJobToSchedule(job)).asJson.noSpaces))

  private def getJobsFromDatabase(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] =
    storage.findJobsByUser(userLogin)

  private def updateDatabaseWithCache(jobSchedule: JobSchedule): Either[UpdateJobError, UpdateJobResult] =
    storage.updateDatabaseWithCache(jobSchedule)
}

object StationService {
  def apply(jobState: JobsState, storage: Storage) = new StationService(jobState, storage)
}
