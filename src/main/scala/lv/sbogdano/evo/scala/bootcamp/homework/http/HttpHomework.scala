package lv.sbogdano.evo.scala.bootcamp.homework.http

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Blocker, Clock, Concurrent, ExitCode, IO, IOApp, Timer}
import cats.implicits._
import io.circe.Json
import lv.sbogdano.evo.scala.bootcamp.homework.http.GuessGame._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Method, Request, ResponseCookie}

import scala.concurrent.ExecutionContext
import scala.util.Random


// Homework. Place the solution under `http` package in your homework repository.
//
// Write a server and a client that play a number guessing game together.
//
// Communication flow should be as follows:
// 1. The client asks the server to start a new game by providing the minimum and the maximum number that can
//    be guessed, as well as the maximum number of attempts.
// 2. The server comes up with some random number within the provided range.
// 3. The client starts guessing the number. Upon each attempt, the server evaluates the guess and responds to
//    the client, whether the current number is lower, greater or equal to the guessed one.
// 4. The game ends when the number is guessed or there are no more attempts left. At this point the client
//    should terminate, while the server may continue running forever.
// 5. The server should support playing many separate games (with different clients) at the same time.
//
// Use HTTP or WebSocket for communication. The exact protocol and message format to use is not specified and
// should be designed while working on the task.

object GuessGame {

  sealed trait EvaluatedRespond

  final case object Lower extends EvaluatedRespond

  final case object Greater extends EvaluatedRespond

  final case object Equal extends EvaluatedRespond

  final case class User(id: Long)

  final case class StartGame(minGuessNumber: Int, maxGuessNumber: Int, maxAttempt: Int)

  final case class ClientGuess(number: Int)

  final case class ServerEvaluatedResponds(evaluatedRespond: EvaluatedRespond)


}

object GuessServer extends IOApp {

  trait Cache[F[_], K, V] {
    def get(key: K): F[Option[V]]

    def put(key: K, value: V): F[Unit]
  }

  class RefCache[F[_] : Clock : Monad, K, V](state: Ref[F, Map[K, V]]) extends Cache[F, K, V] {

    def get(key: K): F[Option[V]] = {
      for {
        map <- state.get
        value = map.get(key)
      } yield value
    }

    def put(key: K, value: V): F[Unit] = {
      for {
        _ <- state.update(f => f ++ Map(key -> value))
      } yield ()
    }
  }

  object Cache {
    def of[F[_] : Clock, K, V]()(implicit T: Timer[F], C: Concurrent[F]): F[Cache[F, K, V]] = {
      for {
        ref <- Ref.of[F, Map[K, V]](Map.empty[K, V])
      } yield {
        new RefCache[F, K, V](ref)
      }
    }
  }


  private val GUESS_GAME = "guessgame"
  private val START_GAME = "startgame"
  private val CLIENT_GUESS_NUMBER = "clientguessnumber"

  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.http4s.circe.CirceEntityCodec._


  def startGameRoute(cache: Cache[IO, String, Json]): HttpRoutes[IO] = {

    val id = java.util.UUID.randomUUID.toString

    HttpRoutes.of[IO] {
      // curl "localhost:9001/guessgame/startgame"
      case req @ GET -> Root / GUESS_GAME / START_GAME =>
        req.as[StartGame].flatMap { startGame =>
          cache.put(id, startGame.asJson)
        }
        Ok().map(_.addCookie(ResponseCookie("id", id)))
    }
  }

  def guessNumberRoute(cache: Cache[IO, String, Json]): HttpRoutes[IO] = {

    //val serverNumber: Int = Random.between(startGame.minGuessNumber, startGame.maxGuessNumber + 1)

    HttpRoutes.of[IO] {

      // curl -XPOST "localhost:9001/guessgame/clientguessnumber" -d '{"number": "3"}' -H "Content-Type: application/json"
      case req@POST -> Root / GUESS_GAME / CLIENT_GUESS_NUMBER =>
        req.as[ClientGuess].flatMap { clientGuess =>

//          clientGuess.number match {
//            case n if n == serverNumber => Ok(ServerEvaluatedResponds(Equal).asJson)
//            case n if n < serverNumber => Ok(ServerEvaluatedResponds(Lower).asJson)
//            case n if n > serverNumber => Ok(ServerEvaluatedResponds(Greater).asJson)
//          }
          Ok()
        }
    }
  }

  private[http] def httpApp(cache: Cache[IO, String, Json]) = {
    startGameRoute(cache) <+> guessNumberRoute(cache)
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      cache <- Cache.of[IO, String, Json]()
      exitCode <- {
        BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9001, host = "localhost")
          .withHttpApp(httpApp(cache))
          .serve
          .compile
          .lastOrError
      }
    } yield exitCode

  }
}

object GuessClient extends IOApp {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._

  private val uri = uri"http://localhost:9001"
  private val GUESS_GAME = "guessgame"
  private val START_GAME = "startgame"
  private val CLIENT_GUESS_NUMBER = "clientguessnumber"
  private val id = java.util.UUID.randomUUID.toString

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  def startGame(client: Client[IO]): IO[String] = {
    val requestIO: IO[Request[IO]] = Method.POST(StartGame(minGuessNumber = 1, maxGuessNumber = 10, maxAttempt = 3), uri / GUESS_GAME / START_GAME)
    for {
      request <- requestIO
      id      <- client.run(request).use(resp => resp.cookies.find(_.name == "id")
          .fold(IO.raiseError(new NoSuchElementException("Can't find cookie with given id")))(cookie => (cookie.content)))
    } yield id
  }

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .parZip(Blocker[IO]).use { case (client, blocker) =>
      for {
        startGame <- client.expect[StartGame](Method.GET(uri / GUESS_GAME / START_GAME))
        attempts = startGame.maxAttempt
        serverEvaluatedResponds <- client.expect[ServerEvaluatedResponds](Method.POST(
          ClientGuess(Random.between(startGame.minGuessNumber, startGame.maxGuessNumber + 1)), uri / GUESS_GAME / CLIENT_GUESS_NUMBER))
        _ <- printLine(serverEvaluatedResponds.toString)
      } yield ()
    }.as(ExitCode.Success)
}



