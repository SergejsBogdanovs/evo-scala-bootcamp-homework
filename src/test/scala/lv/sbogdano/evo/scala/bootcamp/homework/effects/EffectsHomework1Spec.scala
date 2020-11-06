package lv.sbogdano.evo.scala.bootcamp.homework.effects

import lv.sbogdano.evo.scala.bootcamp.homework.effects.EffectsHomework1.IO
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class EffectsHomework1Spec extends AnyFlatSpec {


  "IO[Int](1).map " should "return IO[String](1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1).map(_.toString)
    } yield assert(expected == actual)
  }

  "IO[Int](1).flatMap " should "return IO[String](1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1).flatMap(i => IO(i.toString))
    } yield assert(expected == actual)
  }

  "IO[Int](1) *> IO[String](1) " should "return IO[String](1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1) *> IO("1")
    } yield assert(expected == actual)
  }

  "IO[Int](1) as 1[String]" should "return IO[String](1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1) as "1"
    } yield assert(expected == actual)
  }

  "IO[Int](1) void" should "return IO[Unit]" in {
    for {
      expected <- IO(())
      actual   <- IO(1).void
    } yield assert(expected == actual)
  }

  "IO[Int](1) attempt" should "return IO(Right(1))" in {
    for {
      expected <- IO(Right(1))
      actual   <- IO(1).attempt
    } yield assert(expected == actual)
  }

  "IO(new Throwable) attempt" should "return IO(Left(err))" in {
    for {
      expected <- IO(Left("Error"))
      actual   <- IO(new IllegalArgumentException).attempt
    } yield assert(expected == actual)
  }

  "IO[Int](1) option" should "return IO(Some(1))" in {
    for {
      expected <- IO(Some(1))
      actual   <- IO(1).option
    } yield assert(expected == actual)
  }

  "IO(new Throwable) option" should "return IO(None)" in {
    for {
      expected <- IO(None)
      actual   <- IO(new IllegalArgumentException).option
    } yield assert(expected == actual)
  }

  "IO(new IllegalArgumentException) handleErrorWith" should "return IO(Throwable)" in {
    for {
      expected <- IO(new Throwable)
      actual   <- IO(new IllegalArgumentException).handleErrorWith(_ => IO(new Throwable))
    } yield assert(expected == actual)
  }

  "IO[Int](1) redeem" should "return IO(1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1).redeem(err => "Error", i => i.toString)
    } yield assert(expected == actual)
  }

  "IO(new Throwable) redeem" should "return IO(\"Error\")" in {
    for {
      expected <- IO("Error")
      actual   <- IO(new IllegalArgumentException).redeem(err => "Error", i => i.toString)
    } yield assert(expected == actual)
  }

  "IO[Int](1) redeemWith" should "return IO(1)" in {
    for {
      expected <- IO("1")
      actual   <- IO(1).redeemWith(err => IO("Error"), i => IO(i.toString))
    } yield assert(expected == actual)
  }

  "IO(new Throwable) redeemWith" should "return IO(\"Error\")" in {
    for {
      expected <- IO("Error")
      actual   <- IO(new IllegalArgumentException).redeemWith(err => IO("Error"), i => IO(i.toString))
    } yield assert(expected == actual)
  }

  "IO[Int](1) unsafeRunSync" should "return 1" in {
    assert(1 == IO(1).unsafeRunSync())
  }

  "IO[Int](1) unsafeToFuture" should "return Future(1)" in {
    for {
      expected <- Future(1)
      actual   <- IO(1).unsafeToFuture()
    } yield assert(expected == actual)
  }

  "IO.apply(1) " should "return IO(1)" in {
    for {
      actual <- IO.apply(1)
    } yield assert(1 == actual)
  }

  "IO.delay(1) " should "return IO(1)" in {
    for {
      actual <- IO.delay(1)
    } yield assert(1 == actual)
  }

  "IO.pure(1) " should "return IO(1)" in {
    for {
      actual <- IO.pure(1)
    } yield assert(1 == actual)
  }

  "IO.suspend(1) " should "return IO(1)" in {
    for {
      actual <- IO.suspend(IO(1))
    } yield assert(1 == actual)
  }

  "IO.fromEither(Right(1)) " should "return IO(1)" in {
    for {
      expected <- IO(1)
      actual   <- IO.fromEither(Right(1))
    } yield assert(expected == actual)
  }

  "IO.fromEither(Left(Throwable))" should "throw Throwable" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.fromEither[Throwable](Left(new IllegalArgumentException))
    } yield assert(expected == actual)
  }

  "IO.fromOption(Some(1)) " should "return IO(1)" in {
    for {
      expected <- IO(1)
      actual   <- IO.fromOption(Some(1))(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.fromOption(None)" should "throw Throwable" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.fromOption[Throwable](None)(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.fromTry(Success(1)) " should "return IO(1)" in {
    for {
      expected <- IO(1)
      actual   <- IO.fromTry(Success(1))
    } yield assert(expected == actual)
  }

  "IO.fromTry(Failure(Throwable))" should "throw Throwable" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.fromTry[Throwable](Failure(new IllegalArgumentException))
    } yield assert(expected == actual)
  }

  "IO.none[1] " should "return IO(None)" in {
    for {
      expected <- IO(None)
      actual   <- IO.none[1]
    } yield assert(expected == actual)
  }

  "IO.raiseError" should "throw Throwable" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.raiseError[Throwable](new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.raiseUnless(true)" should "return IO[Unit]" in {
    for {
      expected <- IO(())
      actual   <- IO.raiseUnless(cond = true)(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.raiseUnless(false)" should "return IO[Throwable]" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.raiseUnless(cond = false)(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.raiseWhen(true)" should "return IO[Throwable]" in {
    for {
      expected <- IO(new IllegalArgumentException)
      actual   <- IO.raiseWhen(cond = true)(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.raiseWhen(false)" should "return IO[Unit]" in {
    for {
      expected <- IO(())
      actual   <- IO.raiseWhen(cond = false)(new IllegalArgumentException)
    } yield assert(expected == actual)
  }

  "IO.unlessA(true)" should "return IO[Unit]" in {
    for {
      expected <- IO(())
      actual   <- IO.unlessA(cond = true)(action = IO(println("Hello")))
    } yield assert(expected == actual)
  }

  "IO.unlessA(false)" should "return action" in {
    for {
      expected <- IO(println("Hello"))
      actual   <- IO.unlessA(cond = false)(action = IO(println("Hello")))
    } yield assert(expected == actual)
  }

  "IO.when(false)" should "return IO[Unit]" in {
    for {
      expected <- IO(())
      actual   <- IO.whenA(cond = false)(action = IO(println("Hello")))
    } yield assert(expected == actual)
  }

  "IO.when(true)" should "return action" in {
    for {
      expected <- IO(println("Hello"))
      actual   <- IO.whenA(cond = true)(action = IO(println("Hello")))
    } yield assert(expected == actual)
  }

  "IO.unit" should "return IO(())" in {
    for {
      expected <- IO(())
      actual   <- IO.unit
    } yield assert(expected == actual)
  }

}
