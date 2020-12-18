package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job

case class JobEntity(
                    id: Int,
                    userLogin: String,
                    status: String,
                    priority: String,
                    station: String
                    )
