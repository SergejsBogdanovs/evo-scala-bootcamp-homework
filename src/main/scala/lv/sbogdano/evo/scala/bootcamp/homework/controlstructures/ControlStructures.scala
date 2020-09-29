package lv.sbogdano.evo.scala.bootcamp.homework.controlstructures

import lv.sbogdano.evo.scala.bootcamp.homework.controlstructures.ControlStructures.Command._

import scala.io.Source

object ControlStructures {

  // Homework

  // Create a command line application that reads various "commands" from the
  // stdin, evaluates them, and writes output to stdout.

  // Commands are:

  //   divide 4 5
  // which should output "4 divided by 5 is 0.8"

  //   sum 5 5 6 8.5
  // which should output "the sum of 5 5 6 8.5 is 24.5"

  //   average 4 3 8.5 4
  // which should output "the average of 4 3 8.5 4 is 4.875"

  //   min 4 -3 -17
  // which should output "the minimum of 4 -3 -17 is -17"

  //   max 4 -3 -17
  // which should output "the maximum of 4 -3 -17 is 4"

  // In case of commands that cannot be parsed or calculations that cannot be performed,
  // output a single line starting with "Error: "

  type ErrorMessage = String

  sealed trait Command
  object Command {
    final case class Divide(dividend: Double, divisor: Double) extends Command
    final case class Sum(numbers: List[Double]) extends Command
    final case class Average(numbers: List[Double]) extends Command
    final case class Min(numbers: List[Double]) extends Command
    final case class Max(numbers: List[Double]) extends Command
  }

  sealed trait Result
  case class DivideResult(l: List[Double], result: Double) extends Result
  case class SumResult(l: List[Double], result: Double) extends Result
  case class AverageResult(l: List[Double], result: Double) extends Result
  case class MinResult(l: List[Double], result: Double) extends Result
  case class MaxResult(l: List[Double], result: Double) extends Result

  private def parseCommand(x: String): Either[ErrorMessage, Command] = {
    if (x == null || x.isEmpty) return Left("Error: Command must not be null")

    val validCommands = Set("divide", "sum", "average", "min", "max")
    val split = x.split("\\s")
    val command = split.head
    val numbersStr = split.tail

    if (!numbersStr.map(_.toDoubleOption).forall(_.isDefined)) return Left("Error: Evaluation must contain only numbers")

    val numbers = numbersStr.map(_.toDouble).toList

    if (!validCommands.contains(command)) Left("Error: Invalid command") else command match {
      case "divide" => numbers.length match {
        case 2 => Right(Divide(numbers.head, numbers(1)))
        case _ => Left("Error: Divide operation requires 2 numbers")
      }
      case "sum"     => Right(Sum(numbers))
      case "average" => Right(Average(numbers))
      case "min"     => Right(Min(numbers))
      case "max"     => Right(Max(numbers))
    }
  }

  // should return an error (using `Left` channel) in case of division by zero and other
  // invalid operations
  private def calculate(x: Command): Either[ErrorMessage, Result] = x match {
    case Divide(_, 0) => Left("Error: division by zero")
    case Divide(x, y) => Right(DivideResult(x::y::Nil, x / y))

    case Sum(Nil) => Left("Error: no numbers to evaluate sum")
    case Sum(l)   => Right(SumResult(l, l.sum))

    case Average(Nil) => Left("Error: no numbers to evaluate average")
    case Average(l)   => Right(AverageResult(l, l.sum / l.length))

    case Min(Nil) => Left("Error: no numbers to evaluate min")
    case Min(l)   => Right(MinResult(l, l.min))

    case Max(Nil) => Left("Error: no numbers to evaluate max")
    case Max(l)   => Right(MaxResult(l, l.max))
  }

  private def renderResult(x: Result): String = x match {
    case DivideResult(l, result)  => formatResult("divide", l, result)
    case SumResult(l, result)     => formatResult("sum", l, result)
    case AverageResult(l, result) => formatResult("average", l, result)
    case MinResult(l, result)     => formatResult("minimum", l, result)
    case MaxResult(l, result)     => formatResult("maximum", l, result)
  }

  private def formatResult(name: String, numbers: List[Double], result: Double): String = name match {
    case "divide" =>
      val retString = "%1.0f divided by %1.0f is %1.1f"
      retString.format(numbers.head, numbers(1), result)
    case _ =>
      val retString = "the %s of %s is %1.1f"
      val s = numbers.mkString(" ")
      retString.format(name, s, result)
  }

  private def process(x: String): String = {
    // this will enable useful operations on Either-s such as `leftMap`
    // (map over the Left channel) and `merge` (convert `Either[A, A]` into `A`),
    // but you can also avoid using them using  pattern matching.
      //import cats.implicits._

    // implement using a for-comprehension
    (for {
      command <- parseCommand(x)
      result <- calculate(command)
    } yield result) match {
      case Right(result) => renderResult(result)
      case Left(errorMessage) => errorMessage
    }
  }

  // This `main` method reads lines from stdin, passes each to `process` and outputs the return value to stdout
  def main(args: Array[String]): Unit = Source.stdin.getLines map process foreach println
}

