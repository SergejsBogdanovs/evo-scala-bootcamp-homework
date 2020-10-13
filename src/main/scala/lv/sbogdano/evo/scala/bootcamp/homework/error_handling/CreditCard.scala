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
    ??? // Add errors as needed
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
