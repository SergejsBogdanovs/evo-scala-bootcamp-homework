package lv.sbogdano.evo.scala.bootcamp.homework.adt

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.adt.HoldemType.{Omaha, Texas}

case class Hand private(handCards: List[Card])
object Hand {
  def from(handCards: List[Card], holdemType: HoldemType): Either[ErrorMessage, Hand] = holdemType match {
    case Texas => handCards match {
      case Nil                                => ErrorMessage("Invalid hand cards length").asLeft
      case handCards if handCards.length != 2 => ErrorMessage(s"Hand cards count must be 2").asLeft
      case _                                  => Hand(handCards).asRight
    }
    case Omaha => handCards match {
      case Nil                                => ErrorMessage("Invalid hand cards length").asLeft
      case handCards if handCards.length != 4 => ErrorMessage(s"Hand cards count must be 4").asLeft
      case _                                  => Hand(handCards).asRight
    }
  }
}
