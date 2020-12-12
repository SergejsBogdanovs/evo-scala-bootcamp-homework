package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

import cats.data._
import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.AuthResponse.{AuthResponseError, AuthResponseSuccess}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Role.{Admin, Worker}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.io._
import org.http4s.headers.`Set-Cookie`
import org.http4s.{AuthedRequest, _}


// TODO Use cryptobits?
object Auth {

  type AuthError = String
  type Id = String

  val users: Map[Id, Role] = Map(
    "worker" -> Worker,
    "admin" -> Admin
  )

  def getAuthUser(cookie: Option[`Set-Cookie`]): IO[Either[AuthResponseError, Role]] = {
    cookie match {
      case None            => IO(AuthResponseError("Couldn't find the authcookie").asLeft)
      case Some(cookie)    => {
        users.get(cookie.cookie.content) match {
          case Some(role) => role match {
            case Worker => IO(Worker.asRight)
            case Admin  => IO(Admin.asRight)
          }
          case None => IO(AuthResponseError("Couldn't find user by provided cookies").asLeft)
        }
      }
    }
  }

  def authUser: Kleisli[IO, Request[IO], Either[AuthResponseError, Role]] = Kleisli { request: Request[IO] =>

    val cookie = for {
      c <- headers.`Set-Cookie`.from(request.headers).find(sc => sc.cookie.name == "authcookie")
    } yield c

    getAuthUser(cookie)
  }

  def inAuthFailure: AuthedRoutes[AuthResponseError, IO] = Kleisli { request: AuthedRequest[IO, AuthResponseError] =>
    OptionT.liftF(Forbidden(request.context))
  }

  def verifyLogin(req: Request[IO]): IO[Either[AuthResponseError, Role]] = {
    req.as[User].flatMap(user =>
      if (user.login == "worker" && user.password == "worker") {
        IO(Worker.asRight)
      } else if (user.login == "admin" && user.password == "admin")
        IO(Admin.asRight)
      else {
        IO(AuthResponseError("Invalid user").asLeft)
      }
    )
  }

  def loginUser: Kleisli[IO, Request[IO], Response[IO]] = Kleisli { request: Request[IO] =>
    verifyLogin(request).flatMap {
      case Left(error) => Forbidden(error.asJson)
      case Right(role) => Ok(AuthResponseSuccess("Logged in").asJson).map(_.addCookie(ResponseCookie("authcookie", role.toString)))
    }
  }

}
