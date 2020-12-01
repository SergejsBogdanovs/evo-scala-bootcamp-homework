package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

sealed trait Role
case class User(id: Long, name: String) extends Role
case class Admin(id: Long, name: String) extends Role
