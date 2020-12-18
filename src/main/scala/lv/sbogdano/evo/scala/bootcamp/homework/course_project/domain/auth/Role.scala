package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.auth

sealed trait Role
case object Admin extends Role {
  override def toString: String = "admin"
}
case object Worker extends Role {
  override def toString: String = "worker"
}

case class User(login: String, password: String)
