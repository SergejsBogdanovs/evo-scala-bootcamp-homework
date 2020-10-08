package lv.sbogdano.evo.scala.bootcamp.homework.adt

import cats.implicits.catsSyntaxEitherId
import lv.sbogdano.evo.scala.bootcamp.homework.adt.Holdem.ErrorMessage

case class Board private(input: String) extends AnyVal
object Board {
  def from(input: List[String]): Either[ErrorMessage, Board] = input match {
    case Nil                              => ErrorMessage(s"Invalid input $input").asLeft
    case board :: _ if board.length != 10 => ErrorMessage(s"Board cards count for input $input must be 10").asLeft
    case board :: _                       => Board(board).asRight
  }
}


    