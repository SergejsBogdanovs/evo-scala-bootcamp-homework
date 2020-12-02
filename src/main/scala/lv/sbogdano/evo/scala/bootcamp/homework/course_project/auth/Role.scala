package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

sealed trait Role
object Role {
  case object User extends Role
  case object Admin extends Role
}
