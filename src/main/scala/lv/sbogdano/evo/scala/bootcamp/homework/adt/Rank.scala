package lv.sbogdano.evo.scala.bootcamp.homework.adt

sealed trait Rank {
  def rank: String
  def strength: Int
}
object Rank {
  case object A extends Rank {val rank = "A"; val strength = 14}
  case object K extends Rank {val rank = "K"; val strength = 13}
  case object Q extends Rank {val rank = "Q"; val strength = 12}
  case object J extends Rank {val rank = "J"; val strength = 11}
  case object T extends Rank {val rank = "T"; val strength = 10}
  case object Nine extends Rank {val rank = "9"; val strength = 9}
  case object Eight extends Rank {val rank = "8"; val strength = 8}
  case object Seven extends Rank {val rank = "7"; val strength = 7}
  case object Six extends Rank {val rank = "6"; val strength = 6}
  case object Five extends Rank {val rank = "5"; val strength = 5}
  case object Four extends Rank {val rank = "4"; val strength = 4}
  case object Three extends Rank {val rank = "3"; val strength = 3}
  case object Two extends Rank {val rank = "2"; val strength = 2}

  def from(value: String): Option[Rank] = value match {
    case A.rank => Some(A)
    case K.rank => Some(K)
    case Q.rank => Some(Q)
    case J.rank => Some(J)
    case T.rank => Some(T)
    case Nine.rank => Some(Nine)
    case Eight.rank => Some(Eight)
    case Seven.rank => Some(Seven)
    case Six.rank => Some(Six)
    case Five.rank => Some(Five)
    case Four.rank => Some(Four)
    case Three.rank => Some(Three)
    case Two.rank => Some(Two)
    case _ => None
  }
}

