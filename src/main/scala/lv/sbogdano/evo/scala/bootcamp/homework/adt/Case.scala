package lv.sbogdano.evo.scala.bootcamp.homework.adt

case class Case(board: Board, hands: List[Hand]) {
  def ranked(): List[PokerCombination] = ??? // returns List of PokerCombination according to Board + Hand
}
