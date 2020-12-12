package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity

case class Job(
              id: Long,
              userLogin: String,
              status: Status,
              priority: Priority,
              station: StationEntity
              )
