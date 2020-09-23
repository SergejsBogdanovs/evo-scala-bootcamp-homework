package lv.sbogdano.evo.scala.bootcamp.homework.basics

import scala.math.abs

object Basic {

  def lcm(a: Int, b: Int): Int = abs(a * b) / gcd(a, b)

  def gcd(a: Int, b: Int): Int = b match {
    case 0 => a
    case _ => gcd(b, a % b)
  }

}
