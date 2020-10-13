package lv.sbogdano.evo.scala.bootcamp.homework.error_handling

import cats.data.ValidatedNec

// Homework. Place the solution under `error_handling` package in your homework repository.
//
// 1. Model `CreditCard` class as an ADT (protect against invalid data as much as it makes sense).
// 2. Add `ValidationError` cases (at least 5, may be more).
// 3. Implement `validate` method to construct `CreditCard` instance from the supplied raw data.
object CreditCard {

  final case class CreditCardHolderName(name: String) extends AnyVal
  final case class CreditCardNumber(number: String) extends AnyVal
  final case class CreditCardExpirationDate(expirationDate: String) extends AnyVal
  final case class CreditCardSecurityCode(securityCode: String) extends AnyVal

  case class CreditCard(
                       name: CreditCardHolderName,
                       number: CreditCardNumber,
                       expirationDate: CreditCardExpirationDate,
                       securityCode: CreditCardSecurityCode
                       )

  sealed trait ValidationError
  object ValidationError {
    final case object CardHolderNameLengthIsInvalid extends ValidationError {
      override def toString: String = "Card holder name must be between 3 and 30 characters"
    }

    final case object CardHolderNameSpecialCharacters extends ValidationError {
      override def toString: String = "Card holder name cannot contain special characters"
    }

    final case object CreditCardNumberNotNumeric extends ValidationError {
      override def toString: String = "Credit card number must be a number"
    }

    final case object CreditCardNumberLengthIsInvalid extends ValidationError {
      override def toString: String = "Credit card number length must 16 characters"
    }

    final case object CreditCardExpirationDateNotNumeric extends ValidationError {
      override def toString: String = "Credit card expiration date must be a number"
    }

    final case object CreditCardExpirationDateLengthIsInvalid extends ValidationError {
      override def toString: String = "Credit card expiration date length must 4 characters"
    }

    final case object CreditCardExpirationDateFormatIsInvalid extends ValidationError {
      override def toString: String = "Credit card expiration date format must be MM/YY"
    }

    final case object CreditCardSecurityCodeLengthIsInvalid extends ValidationError {
      override def toString: String = "Credit card security code length must 3 characters"
    }

    final case object CreditCardSecurityCodeFormatNotNumeric extends ValidationError {
      override def toString: String = "Credit card security code must be a number"
    }
  }

  object CreditCardValidator {

    type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

    def validate(
                  name: String,
                  number: String,
                  expirationDate: String,
                  securityCode: String,
                ): AllErrorsOr[CreditCard] = ???
  }
}
