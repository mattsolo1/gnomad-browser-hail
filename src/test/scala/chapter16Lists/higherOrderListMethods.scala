package higherOrderListMethods

import collection.mutable.Stack
import org.scalatest._

class MapSpec extends FlatSpec with Matchers {
  "map" should "transform words in a list to uppercase" in {
    val words = List("the", "quick", "brown", "fox")
    words.map(word => word.toUpperCase).head should be ("THE")
  }
}

class FilterSpec extends FlatSpec with Matchers {
  "filter" should "filter even numbers" in {
    (1 to 5).filter(_ % 2 == 0) should be (List(2, 4))
  }
}

class PartitionSpec extends FlatSpec with Matchers {
  "partition" should "partition list by criteria into a tuple of vectors (pass, fail) " in {
    val result = (1 to 5).partition(_ % 2 == 0)
    println(result)
    result should be (
      (Vector(2, 4), Vector(1, 3, 5))
    )
  }
}

class FindSpec extends FlatSpec with Matchers {
  "find" should "find the first occurrence that matches the criteria and box it" in {
    (1 to 5).find(_ % 2 == 0) should be (Some(2))
  }
  "find" should "return None if there is no result" in {
    (1 to 5).find(_ % 10 == 0) should be (None)
  }
}

class FlatMapSpec extends FlatSpec with Matchers {
  "flatMap" should "apply a function returning sequence for each element in list and flatten results" in {
    val numbers = List(1, 2, 3, 4, 5)
    def functionToApply(x: Int): List[Int] = List(x - 1, x, x + 1)
    val flatMapped = numbers.flatMap(functionToApply(_))
    flatMapped should be (List(0, 1, 2, 1, 2, 3, 2, 3, 4, 3, 4, 5, 4, 5, 6))
  }

  it should "be useful with Option class" in {
    def optionFunction(x: Int) = if (x > 2) Some(x) else None
    val numbers = List(1, 2, 3, 4, 5)
    // Compare with regular map
    numbers.map(optionFunction(_)) should be (
      List(None, None, Some(3), Some(4), Some(5))
    )
    // Option class can be considered a sequence that is either empty or has 1 item
    numbers.flatMap(optionFunction(_)) should be (List(3, 4, 5))
  }

  it should "be able to construct a list of pairs such that 1 <= j < i < 5" in {
    val result = (1 to 5).flatMap(
      i => (1 to i).map(j => (i, j))
    )
    result should be (List((1,1), (2,1), (2,2), (3,1), (3,2), (3,3),
    (4,1), (4,2), (4,3), (4,4), (5,1), (5,2), (5,3), (5,4), (5,5)))
  }
}

class ForEachSpec extends FlatSpec with Matchers {
  "foreach" should "be able to add numbers to variable" in {
    var sum = 0
    (1 to 5) foreach (sum += _)
    sum should be (15)
  }
}

class HigherOrderListMethodsSpec extends FlatSpec with Matchers {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

  it should "use placeholder notation" in {
    val words = List("the", "quick", "brown", "fox")
    words.map(_.toUpperCase).head should be ("THE")
  }
}
