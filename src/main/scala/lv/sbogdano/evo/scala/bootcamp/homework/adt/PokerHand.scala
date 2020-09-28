package lv.sbogdano.evo.scala.bootcamp.homework.adt

object PokerHand extends App {

  // Homework. Define an algebraic data type `PokerHand`, which contains a collection of cards, representing a
  // hand in a poker game. Make sure the defined model protects against invalid data. Use value classes and
  // smart constructors as appropriate. Place the solution under `adt` package in your homework repository.
  type Error = String

  val validRanks = Set("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2")
  val validSuites = Set("h", "d", "c", "s")

  val validateCount = (cards: List[Card], pokerHand: PokerHand) => cards match {
    case cards if cards.length == 5 => Right(pokerHand)
    case _ => Left("Invalid card count")
  }

  trait PokerHand

  trait Card {
    val rank: Either[Error, Rank]
    val suite: Either[Error, Suite]
  }

  final case class Rank(rank: String) extends AnyVal
  object Rank {
    def apply(rank: String): Either[Error, Rank] = rank match {
      case rank if validRanks contains rank => Right(new Rank(rank))
      case _ => Left("Invalid rank")
    }
  }

  final case class Suite(suite: String) extends AnyVal
  object Suite {
    def apply(suite: String): Either[Error, Suite] = suite match {
      case suite if validSuites contains suite => Right(new Suite(suite))
      case _ => Left("Invalid suite")
    }
  }

  final case class HigherCard(cards: List[Card]) extends PokerHand
  object HigherCard {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new HigherCard(cards))
  }

  final case class Pair(cards: List[Card]) extends PokerHand
  object Pair {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new Pair(cards))
  }

  final case class TwoPairs(cards: List[Card]) extends PokerHand
  object TwoPairs {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new TwoPairs(cards))
  }

  final case class ThreeOfAKind(cards: List[Card]) extends PokerHand
  object ThreeOfAKind {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new ThreeOfAKind(cards))
  }

  final case class Straight(cards: List[Card]) extends PokerHand
  object Straight {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new Straight(cards))
  }

  final case class Flush(cards: List[Card]) extends PokerHand
  object Flush {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new Flush(cards))
  }

  final case class FullHouse(cards: List[Card]) extends PokerHand
  object FullHouse {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new FullHouse(cards))
  }

  final case class FourOfAKind(cards: List[Card]) extends PokerHand
  object FourOfAKind {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new FourOfAKind(cards))
  }

  final case class StraightFlush(cards: List[Card]) extends PokerHand
  object StraightFlush {
    def apply(cards: List[Card]): Either[Error, PokerHand] = validateCount(cards, new StraightFlush(cards))
  }
}
