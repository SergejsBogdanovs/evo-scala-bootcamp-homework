package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

import cats.data._
import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Role.{Admin, User}
import org.http4s._
import org.http4s.util.CaseInsensitiveString

import scala.util.Try

object Auth {

  type AuthError = String
  type Id = Long

  val users: Map[Id, Role] = Map(
    12345L -> Admin,
    6789L -> User
  )

  val tokens: Map[String, String] = Map(
    "admin" -> "12345",
    "user" -> "6789"
  )

  def getAuthUser(message: Either[Serializable, Long]): IO[Either[AuthError, Role]] = {
    message match {
      case Left(_)      => IO("Invalid token".asLeft)
      case Right(token) => {
        users.get(token) match {
          case Some(role) => role match {
            case User  => IO(User.asRight)
            case Admin => IO(Admin.asRight)
          }
          case None => IO("Not found user by token".asLeft)
        }
      }
    }
  }

  def authUser: Kleisli[IO, Request[IO], Either[AuthError, Role]] = Kleisli { request: Request[IO] =>

    val message = for {
      header  <- request.headers.get(CaseInsensitiveString("Authorization")).toRight("Couldn't find an Authorization header")
      token   <- tokens.get(header.value).toRight("Invalid token")
      message <- Try(token.toLong).toEither
    } yield message

    getAuthUser(message)
  }

  def inAuthFailure: AuthedRoutes[AuthError, IO] = Kleisli { request: AuthedRequest[IO, AuthError] =>
    request.req match {
      case _ => {
        OptionT.pure[IO](Response[IO](Status.Unauthorized))
      }
    }
  }

}
