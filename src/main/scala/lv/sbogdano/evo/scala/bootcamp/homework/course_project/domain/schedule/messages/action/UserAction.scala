package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action

import java.time.Instant

case class UserAction(time: Instant, action: InputAction)
