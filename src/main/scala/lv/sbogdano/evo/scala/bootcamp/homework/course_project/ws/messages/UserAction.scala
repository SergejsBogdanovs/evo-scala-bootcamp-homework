package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import java.time.Instant

case class UserAction(time: Instant, action: InputAction)
