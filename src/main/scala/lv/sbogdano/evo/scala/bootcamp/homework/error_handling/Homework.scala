package lv.sbogdano.evo.scala.bootcamp.homework.error_handling

// Homework. Place the solution under `error_handling` package in your homework repository.
//
// 1. Model `CreditCard` class as an ADT (protect against invalid data as much as it makes sense).
// 2. Add `ValidationError` cases (at least 5, may be more).
// 3. Implement `validate` method to construct `CreditCard` instance from the supplied raw data.
object Homework {

  final case class CreditCardHolderName(name: String) extends AnyVal
  final case class CreditCardNumber(number: Long) extends AnyVal
  final case class CreditCardExpirationDate(expirationDate: String) extends AnyVal
  final case class CreditCardSecurityCode(securityCode: Int) extends AnyVal

  case class CreditCard(
                       name: CreditCardHolderName,
                       number: CreditCardNumber,
                       expirationDate: CreditCardExpirationDate,
                       securityCode: CreditCardSecurityCode
                       )

  sealed trait ValidationError {
    def errorMessage: String
  }
  object ValidationError {
    final case object CardHolderNameLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Card holder name must be between 3 and 30 characters"
    }

    final case object CardHolderNameSpecialCharacters extends ValidationError {
      def errorMessage: String = "Card holder name cannot contain special characters"
    }

    final case object CreditCardNumberNotNumeric extends ValidationError {
      def errorMessage: String = "Credit card number must be a number"
    }

    final case object CreditCardNumberLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card number length must 16 characters"
    }

    final case object CreditCardExpirationDateFormatIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card expiration date format must be MM/YY"
    }

    final case object CreditCardSecurityCodeLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card security code length must 3 characters"
    }

    final case object CreditCardSecurityCodeFormatNotNumeric extends ValidationError {
      def errorMessage: String = "Credit card security code must be a number"
    }
  }

  object CreditCardValidator {

    import cats.data.ValidatedNec
    import cats.syntax.all._
    import ValidationError._

    // ("(?:0[1-9]|1[0-2])/[0-9]{2}")

    type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

    private def validateCardHolderName(name: String): AllErrorsOr[CreditCardHolderName] = {

      def validateCardHolderNameLength: AllErrorsOr[CreditCardHolderName] = {
        if (name.length >= 3 && name.length <= 30) CreditCardHolderName(name).validNec
        else CardHolderNameLengthIsInvalid.invalidNec
      }

      def validateCardHolderNameContent: AllErrorsOr[CreditCardHolderName] = {
        if (name.matches("^[a-zA-Z]+$")) CreditCardHolderName(name).validNec
        else CardHolderNameSpecialCharacters.invalidNec
      }

      validateCardHolderNameLength *> validateCardHolderNameContent
    }

    private def validateCreditCardNumber(number: String): AllErrorsOr[CreditCardNumber] = {

      def validateCreditCardNumberContent: AllErrorsOr[CreditCardNumber] = {
        if (number.forall(_.isDigit)) CreditCardNumber(number.toLong).validNec
        else CreditCardNumberNotNumeric.invalidNec
      }

      def validateCreditCardNumberLength(creditCardNumber: CreditCardNumber): AllErrorsOr[CreditCardNumber] = {
        if (creditCardNumber.number.toString.length == 16) creditCardNumber.validNec
        else CreditCardNumberLengthIsInvalid.invalidNec
      }

      validateCreditCardNumberContent andThen validateCreditCardNumberLength
    }

    private def validateCreditCardExpirationDate(expirationDate: String): AllErrorsOr[CreditCardExpirationDate] = {

      if (expirationDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) CreditCardExpirationDate(expirationDate).validNec
      else CreditCardExpirationDateFormatIsInvalid.invalidNec
    }

    private def validateCreditCardSecurityCode(securityCode: String): AllErrorsOr[CreditCardSecurityCode] = {

      def validateCreditCardSecurityCodeContent: AllErrorsOr[CreditCardSecurityCode] =
        if (securityCode.forall(_.isDigit)) CreditCardSecurityCode(securityCode.toInt).validNec
        else CreditCardSecurityCodeFormatNotNumeric.invalidNec

      def validateCreditCardSecurityCodeLength(creditCardSecurityCode: CreditCardSecurityCode): AllErrorsOr[CreditCardSecurityCode] =
        if (creditCardSecurityCode.securityCode.toString.length == 3) creditCardSecurityCode.validNec
        else CreditCardSecurityCodeLengthIsInvalid.invalidNec

      validateCreditCardSecurityCodeContent andThen validateCreditCardSecurityCodeLength
    }

    def validate(
                  name: String,
                  number: String,
                  expirationDate: String,
                  securityCode: String,
                ): AllErrorsOr[CreditCard] = {

      (validateCardHolderName(name),
        validateCreditCardNumber(number),
        validateCreditCardExpirationDate(expirationDate),
        validateCreditCardSecurityCode(securityCode)
      ).mapN(CreditCard)

    }
  }
}
