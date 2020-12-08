package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

import cats.data._
import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import io.circe.generic.auto._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth.Role.{Admin, Worker}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
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

  def getAuthUser(cookie: Option[`Set-Cookie`]): IO[Either[AuthError, Role]] = {
    cookie match {
      case None            => IO("Couldn't find the authcookie".asLeft)
      case Some(setCookie) => {
        users.get(setCookie.cookie.content) match {
          case Some(role) => role match {
            case Worker => IO(Worker.asRight)
            case Admin  => IO(Admin.asRight)
          }
          case None => IO("Not found user by authcookie".asLeft)
        }
      }
    }
  }

  def authUser: Kleisli[IO, Request[IO], Either[AuthError, Role]] = Kleisli { request: Request[IO] =>

    val cookie = for {
      c <- headers.`Set-Cookie`.from(request.headers).find(sc => sc.cookie.name == "authcookie")
    } yield c

    getAuthUser(cookie)
  }

  def inAuthFailure: AuthedRoutes[AuthError, IO] = Kleisli { request: AuthedRequest[IO, AuthError] =>
    request.req match {
      case _ =>
        OptionT.pure[IO](Response[IO](Status.Unauthorized))
    }
  }

  def verifyLogin(req: Request[IO]): IO[Either[AuthError, Role]] = {
    req.as[User].flatMap(user =>
      if (user.login == "worker" && user.password == "worker") {
        IO(Worker.asRight)
      } else if (user.login == "admin" && user.password == "admin")
        IO(Admin.asRight)
      else {
        IO("Invalid user".asLeft)
      }
    )
  }

  def loginUser: Kleisli[IO, Request[IO], Response[IO]] = Kleisli { request: Request[IO] =>
    verifyLogin(request).flatMap {
      case Left(error) => Forbidden(error)
      case Right(role) => Ok("Logged in").map(_.addCookie(ResponseCookie("authcookie", role.toString)))
    }
  }

}
