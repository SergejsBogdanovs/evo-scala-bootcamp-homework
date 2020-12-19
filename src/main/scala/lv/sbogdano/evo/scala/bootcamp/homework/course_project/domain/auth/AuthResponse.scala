package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.auth

sealed trait AuthResponse
sealed trait AuthError extends Throwable with AuthResponse
case class AuthResponseError(error: String) extends AuthError
case class AuthResponseSuccess(message: String) extends AuthResponse



