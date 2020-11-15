package lv.sbogdano.evo.scala.bootcamp.homework.effects



import cats.effect.IO
import lv.sbogdano.evo.scala.bootcamp.homework.effects.EffectsHomework2.IO.delay

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/*
 * Homework 1. Provide your own implementation of a subset of `IO` functionality.
 *
 * Provide also tests for this functionality in EffectsHomework1Spec (which you should create).
 *
 * Refer to:
 *  - https://typelevel.org/cats-effect/datatypes/io.html
 *  - https://typelevel.org/cats-effect/api/cats/effect/IO$.html
 *  - https://typelevel.org/cats-effect/api/cats/effect/IO.html
 * about the meaning of each method as needed.
 *
 * There are two main ways how to implement IO:
 * - Executable encoding  - express every constructor and operator for our model in terms of its execution
 * - Declarative encoding - express every constructor and operator for our model as pure data in a recursive
 *                          tree structure
 *
 * While the real Cats Effect IO implementation uses declarative encoding, it will be easier to solve this
 * task using executable encoding, that is:
 *  - Add a `private val run: () => A` parameter to the class `IO` private constructor
 *  - Have most of the methods return a `new IO(...)`
 *
 * Ask questions in the bootcamp chat if stuck on this task.
 */
object EffectsHomework2 {
  sealed abstract class IO[+A] {

    def map[B](f: A => B): IO[B] = Map(this, f)

    def flatMap[B](f: A => IO[B]): IO[B] = FlatMap(this, f)

    def *>[B](another: IO[B]): IO[B] = flatMap(_ => another)

    def as[B](newValue: => B): IO[B] = map(_ => newValue)

    def void: IO[Unit] = map(_ => ())

    def attempt: IO[Either[Throwable, A]] = IO(Try(run()).toEither)

    def option: IO[Option[A]] = attempt.map(either => either.toOption)

    def handleErrorWith[AA >: A](f: Throwable => IO[AA]): IO[AA] = attempt.flatMap {
      case Left(exception) => f(exception)
      case Right(value)    => IO.pure(value)
    }

    def redeem[B](recover: Throwable => B, map: A => B): IO[B] = attempt.map(_.fold(recover, map))

    def redeemWith[B](recover: Throwable => IO[B], bind: A => IO[B]): IO[B] = attempt.flatMap(_.fold(recover, bind))

    def unsafeRunSync(): A = run()

    def unsafeToFuture(): Future[A] = Future { run() }

    def run(): A = {
      this match {
        case Pure(a) => a
        case Delay(thunk) => thunk.apply()
        case RaiseError(e) => ???
        case Suspend(thunk) => ???
        case Map(source, f) => ???
        case FlatMap(source, f) => ???
      }
    }
  }

  object IO {
    def apply[A](body: => A): IO[A] = delay(body)

    def suspend[A](thunk: => IO[A]): IO[A] = delay(thunk.run())

    def delay[A](body: => A): IO[A] = Delay(() => body)

    def pure[A](a: A): IO[A] = Pure(a)

    def fromEither[A](e: Either[Throwable, A]): IO[A] = e match {
      case Left(value)  => raiseError(value)
      case Right(value) => IO(value)
    }

    def fromOption[A](option: Option[A])(orElse: => Throwable): IO[A] = option match {
      case None        => raiseError(orElse)
      case Some(value) => IO(value)
    }

    def fromTry[A](t: Try[A]): IO[A] = t match {
      case Failure(exception) => raiseError(exception)
      case Success(value)     => IO(value)
    }

    def none[A]: IO[Option[A]] = pure(None)

    def raiseError[A](e: Throwable): IO[A] = IO(throw e)

    def raiseUnless(cond: Boolean)(e: => Throwable): IO[Unit] = if (cond) unit else raiseError(e)

    def raiseWhen(cond: Boolean)(e: => Throwable): IO[Unit] = if(cond) raiseError(e) else unit

    def unlessA(cond: Boolean)(action: => IO[Unit]): IO[Unit] = if (cond) unit else action

    def whenA(cond: Boolean)(action: => IO[Unit]): IO[Unit] = if (cond) action else unit

    val unit: IO[Unit] = pure(())
  }

  private final case class Pure[+A](a: A) extends IO[A]

  private final case class Delay[+A](thunk: () => A) extends IO[A]

  private final case class RaiseError(e: Throwable) extends IO[Nothing]

  private final case class Suspend[+A](thunk: () => IO[A]) extends IO[A]

  private final case class Map[E, +A](source: IO[E], f: E => A) extends IO[A] {
    def apply(value: E): IO[A] = Pure(f(value))
  }

  private final case class FlatMap[E, +A](source: IO[E], f: E => IO[A]) extends IO[A]

//  private object AttemptIO extends IO[Either[Throwable, Any]] {
//    def apply(a: Any) = Pure(Right(a))
//    def recover(e: Throwable) = Pure(Left(e))
//  }
//
//  final class ErrorHandler[A](fe: Throwable => IO[A]) extends IO[A] {
//    def recover(e: Throwable): IO[A] = fe(e)
//    def apply(a: A): IO[A] = IO.pure(a)
//  }
//
//  final class Redeem[A, B](fe: Throwable => B, fs: A => B) extends IO[B] {
//    def apply(a: A): IO[B] = IO.pure(fs(a))
//    def recover(e: Throwable): IO[B] = IO.pure(fe(e))
//  }
//
//  final class RedeemWith[A, B](fe: Throwable => IO[B], fs: A => IO[B]) extends IO[B] {
//    def apply(a: A): IO[B] = fs(a)
//    def recover(e: Throwable): IO[B] = fe(e)
//  }

}


