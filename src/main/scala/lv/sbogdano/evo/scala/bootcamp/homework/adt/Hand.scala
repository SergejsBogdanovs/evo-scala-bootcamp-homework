package lv.sbogdano.evo.scala.bootcamp.homework.adt

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.adt.Holdem.ErrorMessage
import lv.sbogdano.evo.scala.bootcamp.homework.adt.HoldemType.{Omaha, Texas}

case class Hand private(input: String) extends AnyVal
object Hand {
  def from(input: List[String], holdemType: HoldemType): Either[ErrorMessage, Hand] = holdemType match {
    case Texas => input match {
      case Nil | board :: hand :: _ if hand.length != 2 => ErrorMessage("Hand count must be 2").asLeft
      case _ :: x :: _                         => Hand(x).asRight
    }
    case Omaha => input match {
      case Nil | board :: hand :: _ if hand.length != 4 => ErrorMessage("Hand count must be 4").asLeft
      case _ :: x :: _                         => Hand(x).asRight
    }
  }
}