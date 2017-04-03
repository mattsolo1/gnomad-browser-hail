package higherOrderListMethods

object higherOrderListMethods {
  // page 361
  val words = List("the", "quick", "brown", "fox")
  words.map(_.length)
  words.map(_.toList.reverse.mkString)
  words.foreach()


  // flatMap takes a function returning a list of elements
  // returns the concatenation of all function results
  words.map(_.toList) // returns a list of lists
  words.flatMap(_.toList) // returns single list concatenated
  // construct list of all pairs such that 1 <= j < i < 5
  List.range(1, 5).flatMap(
    i => List.range(1, i).map(j => (i, j))
  )
  var sum = 0
  List(1, 2, 3, 4, 5) foreach (sum += _)

  // filtering
  List(1, 2, 3, 4, 5).filter(_ % 2 == 0)
  words.filter(_.length == 3)
  List(1, 2, 3, 4, 5).partition(_ % 2 == 0)
  List(1, 2, 3, 4, 5).find(_ % 2 == 0)
  List(1, 2, 3, 4, 5).find(_ <= 0)
  List(1, 2, 3, -4, 5).takeWhile(_ > 0)
  words.dropWhile(_.startsWith("t"))
  List(1, 2, 3, -4, 5).span(_ > 0)

  // predicates
  // forall returns true if all elements satisfy p
  def has0Row(m: List[List[Int]]) =
    m.exists(row => row.forall(_ == 0))
  has0Row(List(List(3, 7), List(8, 0), List(3, 1)))
  has0Row(List(List(0, 0), List(0, 0), List(0, 0)))

  // folding lists
  // fold left
  // requires starting value, list of numbers, binary operation
  // accumulator on left, current value on right
  def sum(xs: List[Int]): Int = (0 /: xs) (_ + _)
  def product(xs: List[Int]): Int = (1 /: xs) (_ * _)
  sum(List(1, 2, 3))
  product(List(1, 2, 3, 4))

  (words.head /: words.tail) (_ + " " + _)
  // fold right
  (words.init :\ words.last) (_ + " " + _)

  // using fold to flatten, comparing efficiencies
  val flattenThis = List(List(0, 3), List(2, 6), List(8, 0))

  def flattenRight[T](xss: List[List[T]]) =
    (xss :\ List[T]()) (_ ::: _)
  def flattenLeft[T](xss: List[List[T]]) =
    (List[T]() /: xss) (_ ::: _)

  flattenRight(flattenThis) // faster
  flattenLeft(flattenThis)
}
