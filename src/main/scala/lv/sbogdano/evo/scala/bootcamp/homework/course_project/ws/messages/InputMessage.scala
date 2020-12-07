package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.Status
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.Status.Pending

sealed trait InputMessage {
  val worker: String
}

object InputMessage{

  case class EnterJobSchedule(worker: String, emptyJobSchedule: Map[Status, List[StationEntity]]) extends InputMessage
  case class Help(worker: String)                                   extends InputMessage
  case class ListJobsAll(worker: String)                            extends InputMessage
  case class ListJobsCompleted(worker: String)                      extends InputMessage
  case class ListJobsPending(worker: String)                        extends InputMessage
  case class MarkJobAsCompleted(worker: String, uniqueName: String) extends InputMessage
  case class InvalidInput(worker: String, string: String)           extends InputMessage
  case class Disconnect(worker: String)                             extends InputMessage

  val emptyJobSchedule: Map[Status, List[StationEntity]] = Map(Pending -> List.empty)
  val helpText: String =
    """Commands:
      | /help           - Show all commands
      | /jobs           - Show all jobs
      | /jobs completed - Show completed jobs
      | /jobs pending   - Show pending jobs
      | /done uniqueName - Mark job as completed
    """.stripMargin

  def parse(worker: String, text: String): InputMessage = {
    splitWords(text) match {
      case ("/help", _)              => Help(worker)
      case ("/jobs", "")             => ListJobsAll(worker)
      case ("/jobs", "completed")    => ListJobsCompleted(worker)
      case ("/jobs", "pending")      => ListJobsPending(worker)
      case ("/done", s"$uniqueName") => MarkJobAsCompleted(worker, uniqueName)
      case (s"/$cmd", "")            => InvalidInput(worker, s"unknown command - $cmd")
      case _                         => InvalidInput(worker, "Invalid command")
    }
  }

  private def splitWords(text: String): (String, String) = {
    val trimmedText = text.trim
    val space = trimmedText.indexOf(' ')
    if (space < 0)
      (trimmedText, "")
    else
      (trimmedText.substring(0, space), trimmedText.substring(space + 1).trim)
  }
}
