import org.scalatest._

import org.json4s._
import org.json4s.jackson.JsonMethods._

case class Child(name: String, age: Int, birthdate: Option[java.util.Date])
case class Address(street: String, city: String)
case class Person(name: String, address: Address, children: List[Child])

class Json4sExtractValuesTest extends FlatSpec with Matchers {
  implicit val formats = DefaultFormats // Brings in default date formats etc.
  "Json AST" should "have values extracted by case classes" in {

    val json = parse("""
         { "name": "joe",
           "address": {
             "street": "Bulevard",
             "city": "Helsinki"
           },
           "children": [
             {
               "name": "Mary",
               "age": 5,
               "birthdate": "2004-09-04T18:06:22Z"
             },
             {
               "name": "Mazy",
               "age": 3
             }
           ]
         }
       """)

     val joe = json.extract[Person]
     joe.isInstanceOf[Person] should be (true)
     joe.address.street should be ("Bulevard")
  }

  "LOL" should "lalala" in {
    println(JObject(List(("allele_num",JInt(1)), ("allele_num",JInt(1)), ("allele_num",JInt(1)), ("allele_num",JInt(1)), ("allele_num",JInt(1)))).obj)
  }
}