package lv.sbogdano.evo.scala.bootcamp.homework.error_handling

// Homework. Place the solution under `error_handling` package in your homework repository.
//
// 1. Model `CreditCard` class as an ADT (protect against invalid data as much as it makes sense).
// 2. Add `ValidationError` cases (at least 5, may be more).
// 3. Implement `validate` method to construct `CreditCard` instance from the supplied raw data.
object Homework {

  sealed trait Month {
    val month: String
  }
  object Month {
    final case object January extends Month {val month: String = "01"}
    final case object February extends Month {val month: String = "02"}
    final case object March extends Month {val month: String = "03"}
    final case object April extends Month {val month: String = "04"}
    final case object May extends Month {val month: String = "05"}
    final case object June extends Month {val month: String = "06"}
    final case object July extends Month {val month: String = "07"}
    final case object August extends Month {val month: String = "08"}
    final case object September extends Month {val month: String = "09"}
    final case object October extends Month {val month: String = "10"}
    final case object November extends Month {val month: String = "11"}
    final case object December extends Month {val month: String = "12"}

    def from(month: String): Month = month match {
      case January.month   => January
      case February.month  => February
      case March.month     => March
      case April.month     => April
      case May.month       => May
      case June.month      => June
      case July.month      => July
      case August.month    => August
      case September.month => September
      case October.month   => October
      case November.month  => November
      case December.month  => December
    }
  }
  final case class Year(value: Int) extends AnyVal

  final case class CreditCardHolderName(name: String) extends AnyVal
  final case class CreditCardNumber(number: String) extends AnyVal
  final case class CreditCardExpirationDate(month: Month, year: Year)
  final case class CreditCardSecurityCode(securityCode: String) extends AnyVal

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

    // Card holder name
    final case object CardHolderNameLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Card holder name must be between 3 and 24 characters"
    }
    final case object CardHolderNameSpecialCharacters extends ValidationError {
      def errorMessage: String = "Card holder name cannot contain special characters"
    }

    // Card holder number
    final case object CreditCardNumberNotNumeric extends ValidationError {
      def errorMessage: String = "Credit card number must be a number"
    }
    final case object CreditCardNumberLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card number length must 16 characters"
    }

    // Card holder expiration date
    final case object CreditCardExpirationDateFormatIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card expiration date format must be in MM/YY"
    }

    // Card holder security code
    final case object CreditCardSecurityCodeLengthIsInvalid extends ValidationError {
      def errorMessage: String = "Credit card security code length must 3 characters"
    }
    final case object CreditCardSecurityCodeFormatNotNumeric extends ValidationError {
      def errorMessage: String = "Credit card security code must be a number"
    }
  }

  object CreditCardValidator {

    import ValidationError._
    import cats.data.ValidatedNec
    import cats.syntax.all._

    type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

    private def validateCardHolderName(name: String): AllErrorsOr[CreditCardHolderName] = {

      def validateCardHolderNameLength: AllErrorsOr[CreditCardHolderName] = {
        if (name.length >= 3 && name.length <= 24) CreditCardHolderName(name).validNec
        else CardHolderNameLengthIsInvalid.invalidNec
      }

      def validateCardHolderNameContent: AllErrorsOr[CreditCardHolderName] = {
        if (name.matches("^[a-zA-Z'~`.\\-\\s+]+$")) CreditCardHolderName(name).validNec
        else CardHolderNameSpecialCharacters.invalidNec
      }

      validateCardHolderNameLength *> validateCardHolderNameContent
    }

    private def validateCreditCardNumber(number: String): AllErrorsOr[CreditCardNumber] = {

      def validateCreditCardNumberContent: AllErrorsOr[String] = {
        if (number.forall(_.isDigit)) number.validNec
        else CreditCardNumberNotNumeric.invalidNec
      }

      def validateCreditCardNumberLength: AllErrorsOr[String] = {
        if (number.length == 16) number.validNec
        else CreditCardNumberLengthIsInvalid.invalidNec
      }

      validateCreditCardNumberContent *> validateCreditCardNumberLength map CreditCardNumber
    }

    private def validateCreditCardExpirationDate(expirationDate: String): AllErrorsOr[CreditCardExpirationDate] = {

      if (expirationDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
        val split = expirationDate.split("/").toList
        val month = split.head
        val year = split.last
        CreditCardExpirationDate(Month.from(month), Year(year.toInt)).validNec
      } else {
        CreditCardExpirationDateFormatIsInvalid.invalidNec
      }
    }

    private def validateCreditCardSecurityCode(securityCode: String): AllErrorsOr[CreditCardSecurityCode] = {

      def validateCreditCardSecurityCodeContent: AllErrorsOr[String] =
        if (securityCode.forall(_.isDigit)) securityCode.validNec
        else CreditCardSecurityCodeFormatNotNumeric.invalidNec

      def validateCreditCardSecurityCodeLength: AllErrorsOr[String] =
        if (securityCode.length == 3) securityCode.validNec
        else CreditCardSecurityCodeLengthIsInvalid.invalidNec

      validateCreditCardSecurityCodeContent *> validateCreditCardSecurityCodeLength map CreditCardSecurityCode
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
