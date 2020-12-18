package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity

case class Job(
              id: Int,
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
