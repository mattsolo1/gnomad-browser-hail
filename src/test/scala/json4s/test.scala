import org.scalatest._

import org.json4s.jackson.JsonMethods
import org.json4s.{JObject, JArray, JInt, JString, JDecimal, JField}
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.DefaultFormats

class Json4s extends FlatSpec with Matchers {
  "parse method" should "parse json into an internal AST format" in {
    val input = """{ "numbers": [1, 2, 3, 4] }"""
    val parsed = JsonMethods.parse(input)
    val expected = JObject(List(("numbers", JArray(List(JInt(1), JInt(2), JInt(3), JInt(4))))))
    parsed should be (expected)
  }
  it should "use big decimal for double if option set" in {
    val input = """{"name":"Toy","price":35.35}"""
    val parsed = JsonMethods.parse(input, useBigDecimalForDouble = true)
    val expected = JObject(List(("name", JString("Toy")), ("price", JDecimal(35.35))))
    parsed should be (expected)
  }
  it should "be queryable" in {
    val json = JsonMethods.parse("""
         { "name": "joe",
           "children": [
             {
               "name": "Mary",
               "age": 5
             },
             {
               "name": "Mazy",
               "age": 3
             }
           ]
         }
       """)
    val ages = for {
      JObject(child) <- json
      JField("age", JInt(age)) <- child
    } yield age
    ages should be (List(5, 3))
  }
}
