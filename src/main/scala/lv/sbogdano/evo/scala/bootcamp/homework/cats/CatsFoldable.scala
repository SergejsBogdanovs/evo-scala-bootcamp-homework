package lv.sbogdano.evo.scala.bootcamp.homework.cats
import cats.{Eval, Later, Now}

object CatsFoldable {

  /**
   * This material has taken from:
   * https://typelevel.org/cats/typeclasses/foldable.html
   * https://www.scala-exercises.org/cats/foldable
   * Book: "Scala with Cats" - https://underscore.io/books/scala-with-cats/
   */
  import cats.Foldable
  import cats.instances.list._

  /**
   * The Foldable type class captures the foldLeft and foldRight meth‐ ods we’re used to in sequences like Lists, Vectors etc.
   * Using Foldable, we can write generic folds that work with a variety of sequence types.
   */

  /**
   * To create Foldable type class instance we can use built in apply method
   */
  val foldableInstance: Foldable[List] = Foldable[List]
  //same
  val foldableInstance1: Foldable[List] = Foldable.apply[List]

  /**
   * Foldable[F] is implemented in terms of two basic methods:
   *
   * foldLeft(fa, b)(f) eagerly folds fa from left-to-right (start to finish).
   * foldRight(fa, b)(f) lazily folds fa from right-to-left (finish to start).
   *
   * foldLeft and foldRight are equivalent if our binary operation is associative.
   */

  /**
   * FOLDLEFT is eager. That means it folds elements when foldLeft is declared.
   *
   * foldLeft(fa, b)(f) - where:
   *  b - is starting value, accumulator
   *  fa - source of elements
   *  f - combination operation
   */
  val ints = List(1, 2, 3)
  val result: Int = foldableInstance.foldLeft(ints, 0)(_ + _) // Int: 6
  val result1: String = Foldable[List].foldLeft(ints, "")(_ + _) // String: 123

  val option: Option[Int] = Option(123)
  val resultFoldOption: Int = Foldable[Option].foldLeft(option, 10)(_ * _) // Int: 1230

  /**
   * FOLDRIGHT is lazy. Thant means it folds elements when foldRight is called.
   *
   * foldRight(fa, b)(f)
   *  b - is starting value, accumulator. In foldrRight case Eval type class.
   *  fa - source of elements
   *  f - combination operation
   */

  /** Generic foldRight */
  def foldRight[A, B](fa: F[A], b: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]

  /** Using Eval means folding is always stack safe */
  val result2: Eval[Int] = Foldable[List].foldRight(List(1, 2, 3), Now(0))((x, acc) => Later(x + acc.value))
  result2.value

  /**
    throws StackOverflowError, because when foldRight is called
     it has to puts 100000 elements in stack
     and start folding 100000 -> 99999 -> 99998 etc...
   */
  def bigData = (1 to 100000).to(LazyList)
  bigData.foldRight(0L)(_ + _)

  /** Now we will see that how can we resolve this issue? */
  val eval: Eval[Long] = Foldable[LazyList].foldRight(bigData, Now(0L)) {
    (num, eval) => eval.map(_ + num)
  }
  eval.value // 5000050000L

  /** But rest collections which we use most commonly, come with the stack safety. */
  (1 to 100000).toList.foldRight(0L)(_ + _) // 5000050000L
  (1 to 100000).toVector.foldRight(0L)(_ + _) // 5000050000L


  /**
   * Foldable provides us with a host of useful methods defined on top of foldLeft.
   * Many of these are facsimiles of familiar methods from the stan‐ dard library:
   * find, exists, forall, toList, isEmpty, nonEmpty, and so on:
   */
  Foldable[Option].nonEmpty(Option(42)) // Boolean: true
  Foldable[List].find(List(1, 2, 3))(_ % 2 == 0) // Some(2)

  /**
   * In addition to these familiar methods, Cats provides two methods that make use of Monoids:
   * 1) combineAll (and its alias fold) combines all elements in the sequence using their Monoid;
   * 2) foldMap maps a user‐supplied function over the sequence and com‐ bines the results using a Monoid.
   */

  Foldable[List].combineAll(List(1, 2, 3)) // Int: 6
  Foldable[List].fold(List(1, 2, 3)) // Int: 6

  Foldable[List].foldMap(List(1, 2, 3))(_.toString) //String: "123"

  /**
   * Explicits over Implicits
   * Scala will only use an instance of Foldable if the method isn’t explicitly available on the receiver.
   * For example, the fol‐ lowing code will use the version of foldLeft defined on List:
   */
  List(1, 2, 3).foldLeft(0)(_ + _) // uses foldLeft defined at List

  def sum[F[_]: Foldable](values: F[Int]): Int = values.foldLeft(0)(_ + _) // used Foldable type class

}
