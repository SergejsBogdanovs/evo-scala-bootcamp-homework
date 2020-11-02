package lv.sbogdano.evo.scala.bootcamp.homework.async

import java.lang.Thread.sleep

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object Future1 extends App {
  //  implicit val baseTime: Long = System.currentTimeMillis

  println("starting calculation...")
  val f = Future {
    sleep(Random.nextInt(500))
    42
  }

  println("before onComplete")
  f.onComplete {
    case Success(value) => println(s"Got the callback, meaning = $value")
    case Failure(exception) => exception.printStackTrace
  }

  println("A...")
  sleep(100)
  println("B...")
  sleep(100)
  println("C...")
  sleep(100)
  println("D...")
  sleep(100)
  println("E...")
  sleep(100)
  println("F...")
  sleep(100)

  sleep(2000)
}

