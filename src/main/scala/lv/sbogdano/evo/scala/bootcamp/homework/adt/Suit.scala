package lv.sbogdano.evo.scala.bootcamp.homework.adt

sealed trait Suit {
  def character: String
}
object Suit {
  case object H extends Suit {val character = "h"}
  case object D extends Suit {val character = "d"}
  case object C extends Suit {val character = "c"}
  case object S extends Suit {val character = "s"}

  def from(value: String): Option[Suit] = value match {
    case H.character => Some(H)
    case D.character => Some(D)
    case C.character => Some(C)
    case S.character => Some(S)
    case _ => None
  }
}




