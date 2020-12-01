package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import org.http4s.{AuthedRequest, AuthedRoutes, Header, Request, Response, Status}
import org.http4s.util.CaseInsensitiveString

object Auth {

  type AuthError = String

  val users: Map[String, Role] = Map(
    "user" -> User(1, "user"),
    "admin" -> Admin(2, "admin")
  )

  def getAuthUserFromHeader(value: String): IO[Option[Role]] = {
    IO {
      users.get(value)
    }
  }

  def authUser: Kleisli[IO, Request[IO], Either[AuthError, Role]] = Kleisli { request: Request[IO] =>
    val headerOpt: Option[Header] = request.headers.get(CaseInsensitiveString("Authorization"))
    headerOpt match {
      case Some(header) => getAuthUserFromHeader(header.value).map(_.toRight("Error"))
      case None         => IO.pure("Header Error".asLeft)
    }

  }

  def inAuthFailure: AuthedRoutes[AuthError, IO] = Kleisli { request: AuthedRequest[IO, AuthError] =>
    request.req match {
      case _ => {
        OptionT.pure[IO](Response[IO](Status.Unauthorized))
      }
    }
  }

}
