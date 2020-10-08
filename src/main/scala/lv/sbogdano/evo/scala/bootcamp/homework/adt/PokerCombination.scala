package lv.sbogdano.evo.scala.bootcamp.homework.adt

trait PokerCombination {
  val cards: List[Card]
}
object PokerCombination {
  final case class HigherCard(cards: List[Card]) extends PokerCombination
  final case class Pair(cards: List[Card]) extends PokerCombination
  final case class TwoPair(cards: List[Card]) extends PokerCombination
  final case class ThreeOfAKind(cards: List[Card]) extends PokerCombination
  final case class Straight(cards: List[Card]) extends PokerCombination
  final case class Flush(cards: List[Card]) extends PokerCombination
  final case class FullHouse(cards: List[Card]) extends PokerCombination
  final case class FourOfAKind(cards: List[Card]) extends PokerCombination
  final case class StraightFlush(cards: List[Card]) extends PokerCombination
}
