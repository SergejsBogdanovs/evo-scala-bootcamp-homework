package lv.sbogdano.evo.scala.bootcamp.homework.course_project.service

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.UserJobSchedule
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError

class StationService(storage: Storage) {


  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.createStation(stationEntity)

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]] = storage.updateStation(stationEntity)

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]] = storage.filterStations(name)

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]] = storage.deleteStation(uniqueName)


  // JobsSchedule

  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule] = storage.findJobsByUser(userLogin)

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule] = storage.findJobsByUserAndStatus(userLogin, status)

  def addJobToSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = storage.addJobToSchedule(job)

  def updateJobStatus(userLogin: UserLogin, jobId: Long, status: Status): Either[OutputActionError, UserJobSchedule] = storage.updateJobStatus(userLogin, jobId, status)

  def updateJobPriority(userLogin: UserLogin, jobId: Long, priority: Priority): Either[OutputActionError, UserJobSchedule] = storage.updateJobPriority(userLogin, jobId, priority)

  def deleteJobFroSchedule(job: Job): Either[OutputActionError, UserJobSchedule] = storage.deleteJobFromSchedule(job)
}

object StationService {
  def apply(storage: Storage) = new StationService(storage)
}
