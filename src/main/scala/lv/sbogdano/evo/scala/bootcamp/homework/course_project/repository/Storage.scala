package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository

import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.error.RepositoryOpsError
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Job, Priority, Status}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.UserJobSchedule
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError

trait Storage {


  def createStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def updateStation(stationEntity: StationEntity): IO[Either[RepositoryOpsError, StationEntity]]

  def filterStations(name: String): IO[Either[RepositoryOpsError, List[StationEntity]]]

  def deleteStation(uniqueName: String): IO[Either[RepositoryOpsError, String]]



  def findJobsByUser(userLogin: UserLogin): Either[OutputActionError, UserJobSchedule]

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): Either[OutputActionError, UserJobSchedule]

  def updateJobStatus(userLogin: UserLogin, jobId: Long, status: Status): Either[OutputActionError, UserJobSchedule]

  def updateJobPriority(userLogin: UserLogin, jobId: Long, priority: Priority): Either[OutputActionError, UserJobSchedule]

  def addJobToSchedule(job: Job): Either[OutputActionError, UserJobSchedule]

  def deleteJobFromSchedule(job: Job): Either[OutputActionError, UserJobSchedule]
}
