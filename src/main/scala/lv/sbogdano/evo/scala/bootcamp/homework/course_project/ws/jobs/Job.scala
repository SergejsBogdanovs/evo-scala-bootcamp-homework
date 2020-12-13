package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity

case class Job(
              id: Long,
              userLogin: String,
              status: Status,
              priority: Priority,
              station: StationEntity
              )

object Job {
  implicit val orderingJob: Ordering[Job] = Ordering.fromLessThan {
    (job1: Job, job2: Job) => job1.priority.value < job2.priority.value
  }
}
