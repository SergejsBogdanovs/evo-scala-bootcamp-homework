package lv.sbogdano.evo.scala.bootcamp.homework.course_project.dto

import cats.implicits.catsSyntaxEitherId

import scala.util.{Failure, Success, Try}

final case class Name (objectType: ObjectType, objectNumber: ObjectNumber) {
  override def toString: String = s"${objectType.toString}${objectNumber.toString}"
}

sealed trait ObjectType extends Product

object ObjectType {
  case object TP extends ObjectType 
  case object KTP extends ObjectType
  case object FP extends ObjectType
  case object SP extends ObjectType
  case object AST extends ObjectType
  
  def objectTypes: Map[String, ObjectType] = Set(TP, KTP, FP, SP, AST).map { x => 
    x.productPrefix.toLowerCase -> x
  }.toMap

  def from(objectType: String): Either[ValidationError, ObjectType] = objectTypes.get(objectType.toLowerCase) match {
    case Some(objectType) => objectType.asRight
    case None             => ObjectTypeInvalid.asLeft
  }
}

final case class ObjectNumber private(objectNumber: Int) {
  override def toString: String = s"$objectNumber"
}

object ObjectNumber {
  def from(objectNumber: String): Either[ValidationError, ObjectNumber] = {
    if (objectNumber.nonEmpty && objectNumber.length <= 4) {
      Try(objectNumber.toInt) match {
        case Failure(_)     => ObjectNumberInvalidFormat.asLeft
        case Success(value) => ObjectNumber(value).asRight
      }
    } else {
      ObjectNumberInvalidLength.asLeft
    }
  }
}

