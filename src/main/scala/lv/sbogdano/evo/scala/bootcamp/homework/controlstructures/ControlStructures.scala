package lv.sbogdano.evo.scala.bootcamp.homework.controlstructures

import lv.sbogdano.evo.scala.bootcamp.homework.controlstructures.ControlStructures.Command._

import scala.io.Source
import scala.util.Try

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
  final case class CommandResult(command: Command, evalResult: Double) extends Result

  sealed trait CommandService {
    def split(x: String): Either[ErrorMessage, List[String]]
    def commandName(split: List[String]): Either[ErrorMessage, String]
    def validateCommand(command: String): Either[ErrorMessage, Unit]
    def commandNumbers(split: List[String]): Either[ErrorMessage, List[String]]
    def numbersStrToDouble(numbersStr: List[String]): Either[ErrorMessage, List[Double]]
    def getCommand(commandName: String, numbers: List[Double]): Either[ErrorMessage, Command]
  }

  final case class MyCommandService() extends CommandService {

    def split(x: String): Either[ErrorMessage, List[String]] = {
      Try(x.split(" ").toList).toOption.toRight(left = "Error: The command is null or empty")
    }

    def commandName(split: List[String]): Either[ErrorMessage, String] = {
      split.headOption.toRight(left = "Error: Can not get command")
    }

    def validateCommand(command: String): Either[ErrorMessage, Unit] = {
      val validCommands = Set("divide", "sum", "average", "min", "max")
      if (validCommands.contains(command)) Right() else Left("Error: Invalid command")
    }

    def commandNumbers(split: List[String]): Either[ErrorMessage, List[String]] = {
      Try(split.tail).toOption.toRight(left = "Error: Can not get numbers")
    }

    def numbersStrToDouble(numbersStr: List[String]): Either[ErrorMessage, List[Double]] = {
      Try(numbersStr.map(_.toDouble)).toOption.toRight(left = "Error: Evaluation must contain only numbers")
    }

    def getCommand(commandName: String, numbers: List[Double]): Either[ErrorMessage, Command] = commandName match {
      case "divide" => numbers.length match {
        case 2 => Right(Divide(numbers.head, numbers(1)))
        case _ => Left("Error: Divide operation requires 2 numbers")
      }
      case "sum" => Right(Sum(numbers))
      case "average" => Right(Average(numbers))
      case "min" => Right(Min(numbers))
      case "max" => Right(Max(numbers))
    }
  }

  private def parseCommand(commandService: CommandService, x: String): Either[ErrorMessage, Command] = {
    import commandService._

    for {
      split                <- split(x)
      commandName          <- commandName(split)
      _                    <- validateCommand(commandName)
      commandNumbersStr    <- commandNumbers(split)
      commandNumbersDouble <- numbersStrToDouble(commandNumbersStr)
      command              <- getCommand(commandName, commandNumbersDouble)
    } yield command
  }

  // should return an error (using `Left` channel) in case of division by zero and other
  // invalid operations
  private def calculate(command: Command): Either[ErrorMessage, Result] = command match {
    case Divide(_, 0) => Left("Error: division by zero")
    case Divide(dividend, divisor) => Right(CommandResult(command, dividend / divisor))

    case Sum(Nil) => Left("Error: no numbers to evaluate sum")
    case Sum(numbers)   => Right(CommandResult(command, numbers.sum))

    case Average(Nil) => Left("Error: no numbers to evaluate average")
    case Average(numbers)   => Right(CommandResult(command, numbers.sum / numbers.length))

    case Min(Nil) => Left("Error: no numbers to evaluate min")
    case Min(numbers)   => Right(CommandResult(command, numbers.min))

    case Max(Nil) => Left("Error: no numbers to evaluate max")
    case Max(numbers)   => Right(CommandResult(command, numbers.max))
  }

  private def renderResult(result: Result): String = result match {
    case CommandResult(command, evalResult) => command match {
      case Divide(dividend, divisor) =>
        val retString = "%1.0f divided by %1.0f is %1.1f"
        retString.format(dividend, divisor, evalResult)
      case Sum(numbers)     => formatResult(numbers, evalResult, "sum")
      case Average(numbers) => formatResult(numbers, evalResult, "average")
      case Min(numbers)     => formatResult(numbers, evalResult, "minimum")
      case Max(numbers)     => formatResult(numbers, evalResult, "maximum")
    }
  }

   private def formatResult(numbers: List[Double], evalResult: Double, commandName: String): String = {
    val retString = "the %s of %s is %1.1f"
    val s = numbers.mkString(" ")
    retString.format(commandName, s, evalResult)
  }

  private def process(x: String): String = {
    // this will enable useful operations on Either-s such as `leftMap`
    // (map over the Left channel) and `merge` (convert `Either[A, A]` into `A`),
    // but you can also avoid using them using  pattern matching.
      //import cats.implicits._

    // implement using a for-comprehension
    (for {
      command <- parseCommand(MyCommandService(), x)
      result <- calculate(command)
    } yield result) fold (left => s"$left", right => renderResult(right))
  }

  // This `main` method reads lines from stdin, passes each to `process` and outputs the return value to stdout
  def main(args: Array[String]): Unit = Source.stdin.getLines map process foreach println
}

