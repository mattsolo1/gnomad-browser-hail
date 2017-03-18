object Lists {
  val myList = List('a', 'b', 'c', 'd', 'e')
  myList.last
  myList.head
  myList.init
  myList.reverse
  myList.reverse.init == myList.tail.reverse
  myList.drop(2)
  myList.take(3)
  myList.splitAt(2)
  myList.indices
  List(List(1, 2), List(4, 7, 8)).flatten
  // List(List(1, 2), List(4, 7, 8, List(9, 10))).flatten
  myList.indices.zip(myList)
  val zipped = myList.zipWithIndex
  zipped.unzip

  // Displaying lists
  myList.toString
  myList.mkString("start ", " -- ", " end")
  myList.mkString("[", ", ", "]")
  myList(4)
  val buffer = new StringBuilder
  myList.addString(buffer, "(", ";", ")")

  // Converting lists: iterator, toArray, copyToArray
  val array = myList.toArray
  array.toList
  val startPosition = 3
  // initialize destination array with sufficient size
  val targetArray = new Array[Int](10)
  List(1, 2, 3).copyToArray(targetArray, startPosition)
  targetArray.toList

  // access list elements through iterator
  val myIterator = myList.iterator
  myIterator.next
  myIterator.next
  myIterator.next
  myIterator.next
  myIterator.next
}