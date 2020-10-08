package lv.sbogdano.evo.scala.bootcamp.homework.adt

import cats.implicits.catsSyntaxEitherId


trait PokerCombination {
  val cards: List[Card]
  val hand: Hand
}
object PokerCombination {
  final case class HigherCard(cards: List[Card], hand: Hand) extends PokerCombination
  final case class Pair(cards: List[Card], hand: Hand) extends PokerCombination
  final case class TwoPair(cards: List[Card], hand: Hand) extends PokerCombination
  final case class ThreeOfAKind(cards: List[Card], hand: Hand) extends PokerCombination
  final case class Straight(cards: List[Card], hand: Hand) extends PokerCombination
  final case class Flush(cards: List[Card], hand: Hand) extends PokerCombination
  final case class FullHouse(cards: List[Card], hand: Hand) extends PokerCombination
  final case class FourOfAKind(cards: List[Card], hand: Hand) extends PokerCombination
  final case class StraightFlush(cards: List[Card], hand: Hand) extends PokerCombination

  def from(cards: List[Card], hand: Hand): Either[ErrorMessage, PokerCombination] = cards match {
    case Nil | cards if cards.length != 5 => ErrorMessage("Invalid cards count").asLeft
    case cards => ??? // count poker combination + corresponding hand
  }
}
