package lv.sbogdano.evo.scala.bootcamp.homework.shared_state

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Clock, Concurrent, ExitCode, Fiber, IO, IOApp, Sync, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/*
 * Please implement a Cache which allows concurrent access.
 *
 * Tip: checking expiration could be represented as some infinite process somewhere in background
 *
 * Tip: you can use following structure to get current time suspended in effect : Clock[F].realTime(MILLISECONDS).flatMap(...)
 *
 * Cached items should have an expiration timestamp after which they are evicted.
 *
 * If we will put a value with the same key then it should renew expiration
 */
object SharedStateHomework extends IOApp {

  trait Cache[F[_], K, V] {
    def get(key: K): F[Option[V]]

    def put(key: K, value: V): F[Unit]
  }

  object IosCommon {
    val logger = Slf4jLogger.getLogger[IO]
  }


  class RefCache[F[_] : Clock : Monad, K, V](
                                              state: Ref[F, Map[K, (Long, V)]],
                                              expiresIn: FiniteDuration
                                            ) extends Cache[F, K, V] {

    def get(key: K): F[Option[V]] = {
      for {
        mapValues <- state.get
        value = mapValues.get(key).map(t => t._2)
      } yield value
    }

    def put(key: K, value: V): F[Unit] = {
      state.update(f => f ++ Map(key -> (expiresIn.toMillis, value)))
    }

  }

  object Cache {
    def of[F[_] : Clock, K, V](
                                expiresIn: FiniteDuration,
                                checkOnExpirationsEvery: FiniteDuration
                              )(implicit T: Timer[F], C: Concurrent[F]): F[Cache[F, K, V]] = {

      val currentTime = T.clock.realTime(MILLISECONDS)
      val valueTime: F[Long] = currentTime.map(time => time + expiresIn.toMillis)

      val check = C.uncancelable(T.sleep(checkOnExpirationsEvery))

      for {
        r <- Ref[F].of(Map[K, (Long, V)]())
      } yield {
        val cache = new RefCache[F, K, V](r, expiresIn)
        cache
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      cache <- Cache.of[IO, Int, String](10.seconds, 4.seconds)
      _ <- cache.put(1, "Hello")
      _ <- cache.put(2, "World")
      _ <- cache.get(1).flatMap(s => IO {
        println(s"first key $s")
      })
      _ <- cache.get(2).flatMap(s => IO {
        println(s"second key $s")
      })
      _ <- IO.sleep(12.seconds)
      _ <- cache.get(1).flatMap(s => IO {
        println(s"first key $s")
      })
      _ <- cache.get(2).flatMap(s => IO {
        println(s"second key $s")
      })
      _ <- IO.sleep(12.seconds)
      _ <- cache.get(1).flatMap(s => IO {
        println(s"first key $s")
      })
      _ <- cache.get(2).flatMap(s => IO {
        println(s"second key $s")
      })
    } yield ExitCode.Success
  }
}

