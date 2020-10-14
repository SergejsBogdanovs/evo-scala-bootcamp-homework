package lv.sbogdano.evo.scala.bootcamp.homework.error_handling

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import lv.sbogdano.evo.scala.bootcamp.homework.error_handling.Homework.{CreditCard, CreditCardExpirationDate, CreditCardHolderName, CreditCardNumber, CreditCardSecurityCode, CreditCardValidator}
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
  }
}
