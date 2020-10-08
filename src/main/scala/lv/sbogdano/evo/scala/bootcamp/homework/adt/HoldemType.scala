package lv.sbogdano.evo.scala.bootcamp.homework.adt

sealed trait HoldemType
object HoldemType {
  case object Texas extends HoldemType
  case object Omaha extends HoldemType
}
