package lv.sbogdano.evo.scala.bootcamp.homework.effects

import lv.sbogdano.evo.scala.bootcamp.homework.effects.EffectsHomework1.IO
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class EffectsHomework1Spec extends AnyFlatSpec with Matchers {

  val iae = new IllegalArgumentException
  val throwable = new Throwable

  "IO[Int](1).map " should "return IO[String](1)" in {
    val test = for {
      actual   <- IO(1).map(_.toString)
      expected <- IO("1")
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1).flatMap " should "return IO[String](1)" in {
    val test = for {
      expected <- IO("1")
      actual   <- IO(1).flatMap(i => IO(i.toString))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) *> IO[String](1) " should "return IO[String](1)" in {
    val test = for {
      expected <- IO("1")
      actual   <- IO(1) *> IO("1")
    } yield  actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) as 1[String]" should "return IO[String](1)" in {
    val test = for {
      expected <- IO("1")
      actual   <- IO(1) as "1"
    } yield  actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) void" should "return IO[Unit]" in {
    IO(1).void.unsafeRunSync() shouldBe ()
  }

  "IO[Int](1) attempt" should "return IO(Right(1))" in {
    val test = for {
      expected <- IO(Right(1))
      actual   <- IO(1).attempt
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO(new Throwable) attempt" should "return IO(Left(err))" in {
    val test = for {
      expected <- IO(Left(iae))
      actual   <- IO.raiseError[Throwable](iae).attempt
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) option" should "return IO(Some(1))" in {
    val test = for {
      expected <- IO(Some(1))
      actual   <- IO(1).option
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO(new Throwable) option" should "return IO(None)" in {
    val test = for {
      expected <- IO(None)
      actual   <- IO.raiseError[Throwable](iae).option
    } yield actual shouldBe expected

    test.unsafeRunSync()

  }

  "IO(new IllegalArgumentException) handleErrorWith"   should "return IO(Throwable)" in {
    val test = for {
      expected <- IO(throwable)
      actual   <- IO.raiseError[Throwable](iae).handleErrorWith(_ => IO(throwable))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) redeem" should "return IO(1)" in {
    val test = for {
      expected <- IO("1")
      actual   <- IO(1).redeem(err => "Error", i => i.toString)
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO(new Throwable) redeem" should "return IO(\"Error\")" in {
    val test = for {
      expected <- IO("Error")
      actual   <- IO.raiseError[Throwable](iae).redeem(err => "Error", i => i.toString)
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) redeemWith" should "return IO(1)" in {
    val test = for {
      expected <- IO("1")
      actual   <- IO(1).redeemWith(err => IO("Error"), i => IO(i.toString))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO(new Throwable) redeemWith" should "return IO(\"Error\")" in {
    val test = for {
      expected <- IO("Error")
      actual   <- IO.raiseError[Throwable](iae).redeemWith(err => IO("Error"), i => IO(i.toString))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO[Int](1) unsafeRunSync" should "return 1" in {
    IO(1).unsafeRunSync() shouldBe 1
  }

  "IO[Int](1) unsafeToFuture" should "return Future(1)" in {
    val test: Future[Assertion] = for {
      expected <- Future(1)
      actual   <- IO(1).unsafeToFuture()
    } yield actual shouldBe expected

    Await.result(test, Duration.Inf)
  }

  "IO.apply(1) " should "return IO(1)" in {
    IO.apply(1).unsafeRunSync() shouldBe 1
  }

  "IO.delay(1)" should "return IO(1)" in {
    IO.delay(1).unsafeRunSync() shouldBe 1
  }

  "IO.pure(1)" should "return IO(1)" in {
    IO.pure(1).unsafeRunSync() shouldBe 1
  }

  "IO.suspend(IO(1))" should "return IO(1)" in {
    IO.suspend(IO(1)).unsafeRunSync() shouldBe 1
  }

  "IO.fromEither(Right(1))" should "return IO(1)" in {
    val test = for {
      expected <- IO(1)
      actual   <- IO.fromEither(Right(1))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.fromEither(Left(Throwable))" should "throw Throwable" in {
    assertThrows[IllegalArgumentException] {
      IO.fromEither[Throwable](Left(iae)).unsafeRunSync()
    }
  }

  "IO.fromOption(Some(1)) " should "return IO(1)" in {
    val test = for {
      expected <- IO(1)
      actual   <- IO.fromOption(Some(1))(iae)
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.fromOption(None)" should "throw Throwable" in {
    assertThrows[IllegalArgumentException] {
      IO.fromOption[Throwable](None)(iae).unsafeRunSync()
    }
  }

  "IO.fromTry(Success(1)) " should "return IO(1)" in {
    val test = for {
      expected <- IO(1)
      actual   <- IO.fromTry(Success(1))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.fromTry(Failure(Throwable))" should "throw Throwable" in {
    assertThrows[IllegalArgumentException] {
      IO.fromTry[Throwable](Failure(iae)).unsafeRunSync()
    }
  }

  "IO.none[1] " should "return IO(None)" in {
    val test = for {
      expected <- IO(None)
      actual   <- IO.none[1]
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.raiseError" should "throw Throwable" in {
    assertThrows[IllegalArgumentException] {
      IO.raiseError[Throwable](iae).unsafeRunSync()
    }
  }

  "IO.raiseUnless(true)" should "return IO[Unit]" in {
    val test = for {
      expected <- IO(())
      actual   <- IO.raiseUnless(cond = true)(iae)
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.raiseUnless(false)" should "return IO[Throwable]" in {
    assertThrows[IllegalArgumentException] {
      IO.raiseUnless(cond = false)(iae).unsafeRunSync()
    }
  }

  "IO.raiseWhen(true)" should "return IO[Throwable]" in {
    assertThrows[IllegalArgumentException] {
      IO.raiseWhen(cond = true)(iae).unsafeRunSync()
    }
  }

  "IO.raiseWhen(false)" should "return IO[Unit]" in {
    val test = for {
      expected <- IO(())
      actual   <- IO.raiseWhen(cond = false)(iae)
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.unlessA(true)" should "return IO[Unit]" in {
    val test = for {
      expected <- IO(())
      actual   <- IO.unlessA(cond = true)(action = IO(println("Hello")))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.unlessA(false)" should "return action" in {
    val test = for {
      expected <- IO(println("Hello"))
      actual   <- IO.unlessA(cond = false)(action = IO(println("Hello")))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.when(false)" should "return IO[Unit]" in {
    val test = for {
      expected <- IO(())
      actual   <- IO.whenA(cond = false)(action = IO(println("Hello")))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.when(true)" should "return action" in {
    val test = for {
      expected <- IO(println("Hello"))
      actual   <- IO.whenA(cond = true)(action = IO(println("Hello")))
    } yield actual shouldBe expected

    test.unsafeRunSync()
  }

  "IO.unit" should "return IO(())" in {
    IO.unit.unsafeRunSync() shouldBe ()
  }
}
