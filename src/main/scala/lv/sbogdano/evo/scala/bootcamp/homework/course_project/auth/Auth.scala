package lv.sbogdano.evo.scala.bootcamp.homework.course_project.auth

import cats.data._
import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.io._
import org.http4s.{AuthedRequest, _}
import org.reactormonk.{CryptoBits, PrivateKey}

import java.time.Clock


object Auth {

  type Id = String

  val key: PrivateKey = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  val crypto: CryptoBits = CryptoBits(key)
  val clock: Clock = Clock.systemUTC

  val users: Map[Id, Role] = Map(
    "worker" -> Worker,
    "admin" -> Admin
  )

  def getAuthUser(token: Option[String]): IO[Either[AuthResponseError, Role]] = IO {
    token match {
      case None       => AuthResponseError("Couldn't find the user by given token").asLeft
      case Some(role) => users.get(role).toRight(AuthResponseError("Couldn't find role for this user"))
    }
//    users.get("admin").toRight(AuthResponseError("Couldn't find role for this user"))
  }

  def authUser: Kleisli[IO, Request[IO], Either[AuthResponseError, Role]] = Kleisli { request: Request[IO] =>

    val cookie = for {
      c     <- headers.`Set-Cookie`.from(request.headers).find(sc => sc.cookie.name == "authcookie")
      token <- crypto.validateSignedToken(c.cookie.content)
    } yield token

    getAuthUser(cookie)
  }

  def inAuthFailure: AuthedRoutes[AuthResponseError, IO] = Kleisli { request: AuthedRequest[IO, AuthResponseError] =>
    OptionT.liftF(Forbidden(request.context))
  }

  def verifyLogin(req: Request[IO]): IO[Either[AuthResponseError, Role]] = {
    req.as[User].flatMap(user => IO {
      if (user.login == "worker" && user.password == "worker")
        Worker.asRight
      else if (user.login == "admin" && user.password == "admin")
        Admin.asRight
      else
        AuthResponseError("Invalid user").asLeft
    }
    )
  }

  def loginUser: Kleisli[IO, Request[IO], Response[IO]] = Kleisli { request: Request[IO] =>
    verifyLogin(request).flatMap {
      case Left(error) => Forbidden(error.asJson)
      case Right(role) =>
        val message = crypto.signToken(role.toString, clock.millis.toString)
        Ok(AuthResponseSuccess("Logged in").asJson).map(_.addCookie(ResponseCookie("authcookie", message)))
//        Ok(AuthResponseSuccess("Logged in").asJson).map(_.addCookie(ResponseCookie("authcookie", role.toString)))
    }
  }

}
