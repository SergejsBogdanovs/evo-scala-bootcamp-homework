package lv.sbogdano.evo.scala.bootcamp.homework.controlstructures

import lv.sbogdano.evo.scala.bootcamp.homework.controlstructures.ControlStructures.Command.{Average, Divide, Max, Min, Sum}

import scala.Left
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
  case class Success(value: String) extends Result
  case class Fault(value: String) extends Result

  def parseCommand(x: String): Either[ErrorMessage, Command] = {
    if (x == null || x.isEmpty) return Left("Error: Command must not be null")

    val validCommands = Set("divide", "sum", "average", "min", "max")
    val split = x.split("\\s")
    val command = split.head
    val numbers = split.tail.flatMap(s => Try(s.toDouble).toOption).toList

    if (!validCommands.contains(command)) Left("Error: Invalid command") else command match {
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

  // should return an error (using `Left` channel) in case of division by zero and other
  // invalid operations
  def calculate(x: Command): Either[ErrorMessage, Result] = {
    ??? // implement this
  }

  def renderResult(x: Result): String = {
    ??? // implement this
  }

  def process(x: String): String = {
    // this will enable useful operations on Either-s such as `leftMap`
    // (map over the Left channel) and `merge` (convert `Either[A, A]` into `A`),
    // but you can also avoid using them using pattern matching.
    //import cats.implicits._

    // implement using a for-comprehension
//    for {
//      command <- parseCommand(x)
//      result <- calculate(command)
//      rendered <- renderResult(result)
//    } yield rendered
    ???
  }

  // This `main` method reads lines from stdin, passes each to `process` and outputs the return value to stdout
  def main(args: Array[String]): Unit = Source.stdin.getLines map process foreach println
}

