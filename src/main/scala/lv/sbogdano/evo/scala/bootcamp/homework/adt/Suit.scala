package lv.sbogdano.evo.scala.bootcamp.homework.adt

sealed trait Suit {
  def character: String
}
object Suit {
  case object H extends Suit {val character = "h"}
  case object D extends Suit {val character = "d"}
  case object C extends Suit {val character = "c"}
  case object S extends Suit {val character = "s"}
}




