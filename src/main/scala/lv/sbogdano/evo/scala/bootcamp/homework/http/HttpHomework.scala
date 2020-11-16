package lv.sbogdano.evo.scala.bootcamp.homework.http

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{Ok, _}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder

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
object GuessServer extends IOApp {


  private val jsonRoutes = {
    import GuessGame._
    import org.http4s.circe.CirceEntityCodec._
    import io.circe.generic.auto._
    import io.circe.syntax._

    val GUESS_GAME = "guessgame"
    val START_GAME = "startgame"
    val CLIENT_GUESS_NUMBER = "clientguessnumber"

    val startGame = StartGame(minGuessNumber = 1, maxGuessNumber = 10, maxAttempt = 3)
    val serverNumber = new Random().between(startGame.minGuessNumber, startGame.maxGuessNumber + 1)
    println(serverNumber)

    HttpRoutes.of[IO] {
      // curl "localhost:9001/guessgame/startgame"
      case GET -> Root / GUESS_GAME / START_GAME =>
        Ok(startGame.asJson)

      // curl -XPOST "localhost:9001/guessgame/clientguessnumber" -d '{"number": "3"}' -H "Content-Type: application/json"
      case req @ POST -> Root / GUESS_GAME / CLIENT_GUESS_NUMBER =>
        req.as[ClientGuess].flatMap { clientGuess =>

          clientGuess.number match {
            case n if n == serverNumber => Ok(ServerEvaluatedResponds(Equal).asJson)
            case n if n < serverNumber  => Ok(ServerEvaluatedResponds(Lower).asJson)
            case n if n > serverNumber  => Ok(ServerEvaluatedResponds(Greater).asJson)
          }
        }
    }
  }

  private val httpApp = {
    jsonRoutes
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port = 9001, host = "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}

object GuessClient extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = ???
}

object GuessGame {

  sealed trait EvaluatedRespond
  final case object Lower extends EvaluatedRespond
  final case object Greater extends EvaluatedRespond
  final case object Equal extends EvaluatedRespond

  final case class StartGame(minGuessNumber: Int, maxGuessNumber: Int, maxAttempt: Int)

  final case class ServerNumber(serverNumber: Int)

  final case class ClientGuess(number: Int)

  final case class ServerEvaluatedResponds(evaluatedRespond: EvaluatedRespond)

}

