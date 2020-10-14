package lv.sbogdano.evo.scala.bootcamp.homework.error_handling

import cats.implicits.{catsSyntaxValidatedId, catsSyntaxValidatedIdBinCompat0}
import lv.sbogdano.evo.scala.bootcamp.homework.error_handling.Homework.{CreditCard, CreditCardExpirationDate, CreditCardHolderName, CreditCardNumber, CreditCardSecurityCode, CreditCardValidator, ValidationError}
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class HomeworkSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks{

  "CreditCardValidator" should "handle valid and invalid credit cards" in {
    import lv.sbogdano.evo.scala.bootcamp.homework.error_handling.Homework.ValidationError._

    CreditCardValidator.validate(
      name = "creditcard",
      number = "1234123412341234",
      expirationDate = "09/12",
      securityCode = "123"
    ) shouldBe CreditCard(
        CreditCardHolderName("creditcard"),
        CreditCardNumber(1234123412341234L),
        CreditCardExpirationDate("09/12"),
        CreditCardSecurityCode(123)).validNec

    def checkInvalid(
                    name: String,
                    number: String,
                    expirationDate: String,
                    securityCode: String,
                    errors: Set[ValidationError]
                    ): Assertion = {
      CreditCardValidator.validate(
        name = name,
        number = number,
        expirationDate = expirationDate,
        securityCode = securityCode,
      ).leftMap(_.toChain.toList.toSet) shouldBe errors.invalid
    }

    checkInvalid(
      name = "a",
      number = "1234123412341234",
      expirationDate = "09/12",
      securityCode = "123",
      errors = Set(CardHolderNameLengthIsInvalid)
    )
    checkInvalid(
      name = "a%",
      number = "1234123412341234",
      expirationDate = "09/12",
      securityCode = "123",
      errors = Set(CardHolderNameLengthIsInvalid, CardHolderNameSpecialCharacters)
    )
    checkInvalid(
      name = "a",
      number = "123412341234123",
      expirationDate = "09/12",
      securityCode = "123",
      errors = Set(CardHolderNameLengthIsInvalid, CreditCardNumberLengthIsInvalid)
    )
    checkInvalid(
      name = "a",
      number = "123412341234123s",
      expirationDate = "09/12",
      securityCode = "123",
      errors = Set(CardHolderNameLengthIsInvalid, CreditCardNumberNotNumeric)
    )
    checkInvalid(
      name = "Sergejs",
      number = "1234123412341234",
      expirationDate = "19/12",
      securityCode = "123",
      errors = Set(CreditCardExpirationDateFormatIsInvalid)
    )
    checkInvalid(
      name = "Sergejs",
      number = "1234123412341234",
      expirationDate = "1/12",
      securityCode = "12s",
      errors = Set(CreditCardExpirationDateFormatIsInvalid, CreditCardSecurityCodeFormatNotNumeric)
    )
    checkInvalid(
      name = "Sergejs",
      number = "1234123412341234",
      expirationDate = "01/2",
      securityCode = "1232",
      errors = Set(CreditCardExpirationDateFormatIsInvalid, CreditCardSecurityCodeLengthIsInvalid)
    )
    checkInvalid(
      name = "a",
      number = "1",
      expirationDate = "01/2",
      securityCode = "1232",
      errors = Set(
        CardHolderNameLengthIsInvalid,
        CreditCardNumberLengthIsInvalid,
        CreditCardExpirationDateFormatIsInvalid,
        CreditCardSecurityCodeLengthIsInvalid
        )
    )
  }
}
