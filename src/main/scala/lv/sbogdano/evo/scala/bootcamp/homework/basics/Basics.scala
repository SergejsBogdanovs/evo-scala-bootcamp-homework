package lv.sbogdano.evo.scala.bootcamp.homework.basics

import scala.annotation.tailrec
import scala.math.abs

object Basics extends App {

  def lcm(a: Int, b: Int): Int = abs(a * b) / gcd(a, b)

  @tailrec
  def gcd(a: Int, b: Int): Int = b match {
    case 0 => a
    case _ => gcd(b, a % b)
  }

}
