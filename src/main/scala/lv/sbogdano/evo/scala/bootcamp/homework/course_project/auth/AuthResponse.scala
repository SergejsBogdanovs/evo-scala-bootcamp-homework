package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

sealed trait AuthResponse
case class AuthResponseError(error: String) extends AuthResponse
case class AuthResponseSuccess(message: String) extends AuthResponse



