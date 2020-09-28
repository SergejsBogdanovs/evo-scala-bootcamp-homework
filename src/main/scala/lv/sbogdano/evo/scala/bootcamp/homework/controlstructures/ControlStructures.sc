import scala.util.Try

val x = "divide 4 5"
val split = x.split("\\s")
val command = split.head
val dividendAndDivisor = split.tail

Array("5", "5", "6" ,"8.5", "a").flatMap(s => Try(s.toDouble).toOption).toList

"average" match {
  case "average" => println("Yes")
}
