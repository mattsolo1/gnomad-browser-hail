import org.scalatest._

class LambdaSpec extends FlatSpec with Matchers {
  "Multiple parameter lists" should "are possible" in {
    def sayHelloAgainAgain(name: String)(whoAreYou: () => String) = {
      s"Hello $name, it is ${whoAreYou()}"
    }
    def provideName() = { "Scala" }

    val fast = sayHelloAgainAgain("test")(provideName)
    val faster = sayHelloAgainAgain("test") { () => "Scala!!!"}

    fast should be("Hello test, it is Scala")
    faster should be("Hello test, it is Scala!!!")
  }

  "implicits" should "can be used to fill values in scope" in {
    def saySomething(text: String)(implicit location: String) = {
      s"Saying $text from location $location"
    }

    implicit val someLocation = "Boston"
    saySomething("Hello") should be ("Saying Hello from location Boston")
    val another = saySomething("Not using implicit value")("Cambridge")
    another should be ("Saying Not using implicit value from location Cambridge")
  }
}

abstract class SimplePerson(firstName: String, lastName: String) {
  def fullName = { s"$firstName-$lastName" }
}

case class Student(firstName: String, lastName: String, id: Int) extends SimplePerson(firstName, lastName)

class LambdaPatternMatchingSpec extends FlatSpec with Matchers {
  "Pattern matching" should "example" in {
    val student = Student("John", "Doe", 42)

    def getId[T <: SimplePerson](something: T) = {
      something match {
        case Student(firstName, lastName, id) => s"$firstName-$lastName-$id"
        case p: SimplePerson => p.fullName
      }
    }
    getId(student) should be ("John-Doe-42")
  }
}
 