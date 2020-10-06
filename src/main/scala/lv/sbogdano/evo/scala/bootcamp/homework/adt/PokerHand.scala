package lv.sbogdano.evo.scala.bootcamp.homework.adt

object PokerHand extends App {

  // Homework. Define all algebraic data types, which would be needed to implement “Hold’em Hand Strength”
  // task you completed to join the bootcamp. Use your best judgement about particular data types to include
  // in the solution, you can model concepts like:
  //
  // 1. Suite
  // 2. Rank
  // 3. Card
  // 4. Hand (Texas or Omaha)
  // 5. Board
  // 6. Poker Combination (High Card, Pair, etc.)
  // 7. Test Case (Board & Hands to rank)
  // 8. Test Result (Hands ranked in a particular order)
  //
  // Make sure the defined model protects against invalid data. Use value classes and smart constructors as
  // appropriate. Place the solution under `adt` package in your homework repository.
  type Error = String

  sealed trait Hand
  object Hand {
    case object TexasHoldem extends Hand
    case object OmahaHoldem extends Hand
  }

  sealed trait PokerHand {
    val cards: List[Card]
  }
  object PokerHand {
    case class HigherCard private(cards: List[Card]) extends PokerHand
    case class Pair private(cards: List[Card]) extends PokerHand
    case class TwoPair private(cards: List[Card]) extends PokerHand
    case class ThreeOfAKind private(cards: List[Card]) extends PokerHand
    case class Straight private(cards: List[Card]) extends PokerHand
    case class Flush private(cards: List[Card]) extends PokerHand
    case class FullHouse private(cards: List[Card]) extends PokerHand
    case class FourOfAKind private(cards: List[Card]) extends PokerHand
    case class StraightFlush private(cards: List[Card]) extends PokerHand

    def from(cards: List[Card]): Either[Error, PokerHand] = cards match {
      case Nil => Left("Error: Cards count must be 5")
      case cards if cards.length != 5 => Left("Error: Cards count must be 5")
      case _ => ??? // Calculate Poker combination and return it ex. Right(Pair(cards))
    }
  }

  sealed trait Rank {
    def character: String
    def strength: Int
  }
  object Rank {
    case object A extends Rank {val character = "A"; val strength = 14}
    case object K extends Rank {val character = "K"; val strength = 13}
    case object Q extends Rank {val character = "Q"; val strength = 12}
    case object J extends Rank {val character = "J"; val strength = 11}
    case object T extends Rank {val character = "T"; val strength = 10}
    case object Nine extends Rank {val character = "9"; val strength = 9}
    case object Eight extends Rank {val character = "8"; val strength = 8}
    case object Seven extends Rank {val character = "7"; val strength = 7}
    case object Six extends Rank {val character = "6"; val strength = 6}
    case object Five extends Rank {val character = "5"; val strength = 5}
    case object Four extends Rank {val character = "4"; val strength = 4}
    case object Three extends Rank {val character = "3"; val strength = 3}
    case object Two extends Rank {val character = "2"; val strength = 2}
  }

  sealed trait Suite {
    def character: String
  }
  object Suite {
    case object H extends Suite {val character = "h"}
    case object D extends Suite {val character = "d"}
    case object C extends Suite {val character = "c"}
    case object S extends Suite {val character = "s"}
  }

  case class Card(rank: Rank, suite: Suite)

  val cards = List(Card(Rank.K, Suite.D)).map(c => c.rank.strength -> c.suite.character).toMap
}
