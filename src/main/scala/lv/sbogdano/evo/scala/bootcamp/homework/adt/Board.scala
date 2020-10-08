package lv.sbogdano.evo.scala.bootcamp.homework.adt

import cats.implicits.catsSyntaxEitherId

case class Board private(boardCards: List[Card])
object Board {
  def from(boardCards: List[Card]): Either[ErrorMessage, Board] = boardCards match {
    case Nil                                  => ErrorMessage(s"Invalid input $boardCards").asLeft
    case boardCards if boardCards.length != 5 => ErrorMessage(s"Board cards count must be 5").asLeft
    case _                                    => Board(boardCards).asRight
  }
}



    