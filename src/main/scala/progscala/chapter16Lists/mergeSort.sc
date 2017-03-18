object mergeSortExample {
  // page 359

  def mergeSort[T](less: (T, T) => Boolean)(xs: List[T]): List[T] = {
    def merge(xs: List[T], ys: List[T]): List[T] =
      (xs, ys) match {
        case (Nil, _) => ys
        case (_, Nil) => xs
        case (x :: xs1, y :: ys1) =>
          if (less(x, y)) x :: merge(xs1, ys)
          else y :: merge(xs, ys1)
      }
    val n = xs.length / 2
    // if the list has 0 or 1 elements, already sorted
    if (n == 0) xs
    else {
      // longer lists split into 2 sub-lists with half elements
      val (ys, zs) = xs.splitAt(n)
      // each sub list sorted by recursive call
      // and the results are combined
      merge(mergeSort(less)(ys), mergeSort(less)(zs))
    }
  }
  // use the function
  mergeSort((x: Int, y: Int) => x < y)(List(5, 7, 1, 3))

  // use currying to specialize the function
  val integerSort = mergeSort((x: Int, y: Int) => x < y) _
  val reverseIntegerSort = mergeSort((x: Int, y: Int) => x > y) _

  val mixedIntegers = List(4, 1, 9, 0, 5, 8, 3, 6, 2, 7)

  integerSort(mixedIntegers)
  reverseIntegerSort(mixedIntegers)
}