object divideAndConquer {
  // take 2 lists to be concatenated as parameters
  def append[T](xs: List[T], ys: List[T]): List[T] =
  xs match {
    // the first list is empty so just return the second
    case List() => ys
    // separate the xs head from the tail
    // To construct a list, need to know the head and the tail
    // the head his x, the tail is the result of the recursion
    case x :: xs1 => x :: append(xs1, ys)
  }
  append(List(1, 2, 3), List(7, 8, 9))
}
