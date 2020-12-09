package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.{JobSchedule, UserLogin}

sealed trait OutputAction
object OutputAction {
  case class WelcomeOutputAction(message: String) extends OutputAction
  case class ErrorOutputAction(message: String) extends OutputAction
  case class ListJobsOutputAction(stations: List[StationEntity]) extends OutputAction
  case class AddJobsOutputAction(jobs: Map[UserLogin, JobSchedule]) extends OutputAction
  case class MarkJobAsCompletedOutputAction(jobs: Map[UserLogin, JobSchedule]) extends OutputAction
}

